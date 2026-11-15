package org.singsurf.jns3.network_layer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;

/**
 * Sends IP packets over Ethernet. 
 * It uses ARP to resolve IP addresses to MAC addresses
 * and fragments packets if they are too large for the Ethernet frame.
 * Our implementation uses the low size of 140 bytes.
 */
public class EthernetPacketSender implements PacketSender, TickListener {
	public static boolean logMessages = false;

	ProtocolDevice device;
	IPProtocol ipProtocol;
	FrameSender frameSender;
	PacketFragmentor packetFragmentor = new PacketFragmentor();
	
	Map<IPAddress, TaggedQueue> packetQueue = new HashMap<>();
	
	static class TaggedQueue {
		private Queue<IPPacket> queue;
		private boolean requested;

		public TaggedQueue(Queue<IPPacket> queue, boolean requested) {
			this.queue = queue;
			this.requested = requested;
		}

		public Queue<IPPacket> queue() {
			return queue;
		}

		public boolean requested() {
			return requested;
		}
	}
	
	public EthernetPacketSender(ProtocolDevice device, 
			IPProtocol ipProtocol) {
		this.device = device;
		this.ipProtocol = ipProtocol;
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void send(IPPacket packet) {
		
		var destIP = packet.getDestinationAddress();
		var srcIP = packet.getSourceAddress();
		var isSrcLocal = device.isLocalAddress(srcIP);
		var isDestLocal = device.isLocalAddress(destIP);
		
		if(isSrcLocal && isDestLocal) {
			var srcMac = device.getArpProtocol().lookup(srcIP);
			var destMac = device.getArpProtocol().lookup(destIP);
			if(destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS) 
			 || destIP.equals(device.getMulticastAddress())) {
				destMac = MACAddress.BROADCAST_MAC;
			} 
			
			if(srcMac == null) {
				Jns3Logger.logError(ipProtocol.name, "No ARP entry for source IP " + srcIP + ". Cannot send packet.");
				return;
			}
			if(destMac == null) {
				queuePacket(packet);
				return;
			}
			sendPacket(packet, srcMac, destMac);
			return;
		} else if(isSrcLocal && !isDestLocal) {
			var rp = device.getRoutingProtocol();
			var srcMac = device.getMACAddress();
			MACAddress  destMac;
			if(rp != null) {
				destMac = rp.lookup(destIP);
				var publicIP = rp.getPublicIP(srcIP);
				if(publicIP != null) {
					packet.setSourceIP(publicIP);
				}else {
					Jns3Logger.logError(ipProtocol.name, "No public IP found for source IP " + srcIP + ". Cannot send packet.");
					return;
				}
			} else {
				var gate = device.getDefaultGateway();
				destMac = device.getArpProtocol().lookup(gate);
			}
			if(destMac == null) {
				Jns3Logger.logError(ipProtocol.name, "No RP entry for destination IP " + destIP + ". Cannot send packet.");
				return;
			}
			sendPacket(packet, srcMac, destMac);
			return;
		} else if(!isSrcLocal && isDestLocal) {
			// sent from the routing protocol, with destIP modified to be on the local subnet
			var srcMac = device.getMACAddress();
			var destMac = device.getArpProtocol().lookup(destIP);
			if(destMac == null) {
				queuePacket(packet);
				return;
			}
			sendPacket(packet, srcMac, destMac);
			return;
		} else {
			var rp = device.getRoutingProtocol();
			var srcMac = device.getMACAddress();
			var destMac = rp.lookup(destIP);
			if(destMac == null) {
				
				var errorMsg = ICMPMessage.createHostUnreachable(packet);
				var errorPacket = new IPPacket(
					IPPacket.IMCP, 64,
					device.getIPAddress(), packet.getSourceAddress(), 
					errorMsg.getBytes());
				Jns3Logger.logWarn(device.getName(), "Host unreachable "+packet);
				
				send(errorPacket);
				return;
			}
			if(srcMac == null) {
				Jns3Logger.logError(ipProtocol.name, "No ARP entry for source IP " + srcIP + ". Cannot send packet.");
				return;
			}
			sendPacket(packet, srcMac, destMac);
			return;
		}
	}

	private void queuePacket(IPPacket packet) {
		// No ARP entry for destination IP
		var destIP = packet.getDestinationAddress();
		var queue = packetQueue.computeIfAbsent(destIP,
				k -> new TaggedQueue(new LinkedList<>(),false));
		queue.queue().add(packet);
		if(!queue.requested()) {
			device.getArpProtocol().sendArpRequest(destIP);
			queue.requested = true;
		}
		packetQueue.put(destIP, new TaggedQueue(queue.queue(), true));
		Jns3Logger.logInfo(ipProtocol.name, "No ARP entry for " + destIP + ". Packet queud.");
	}

	private void sendPacket(IPPacket packet, MACAddress srcMac, MACAddress destMac) {
		if(logMessages) {
			Jns3Logger.logSend(ipProtocol.name, srcMac.toString() +" -> " 
					+ destMac.toString() + " " + packet.toShortString());
		}

		List<IPPacket> packets = packetFragmentor.getFragments(packet);
		for(IPPacket p : packets) {
			EthernetFrame frame = new EthernetFrame(
					destMac, srcMac,
					IPProtocol.ETHERTYPE,
					p.getBytes());
			if(!IPPacketExtractor.getInstance().isExtractable(frame)) {
				Jns3Logger.logError(ipProtocol.name, "Frame is not extractable as IP packet. Frame: " + frame);
			}
			frameSender.send(frame);
		}
	}

	@Override
	public void onTick() {
		List<IPAddress> toRemove = new LinkedList<>();
		for(var ent: packetQueue.entrySet()) {
			var destIp = ent.getKey();
			var queue = ent.getValue();
			var size = queue.queue().size();
			var destMac = device.getArpProtocol().lookup(destIp);
			if(destMac != null) {
				while(!queue.queue().isEmpty()) {
					IPPacket packet = queue.queue().poll();
					send(packet);
				}
				toRemove.add(destIp);
				Jns3Logger.logInfo(ipProtocol.name, "Sent "+size+" queued packets to " + destIp + " after ARP resolution.");
			}
		}
		for(var ip: toRemove) {
			if(packetQueue.get(ip).queue().isEmpty())
				packetQueue.remove(ip);
		}
		for(var ip: packetQueue.keySet()) {
			device.getArpProtocol().sendArpRequest(ip);
		}
		
	}

	public void setFragmentSize(int size) {
		packetFragmentor.setMaxLength(size);
		
	}

	public void setFrameSender(FrameSender frameSender2) {
		this.frameSender = frameSender2;		
	}

}
