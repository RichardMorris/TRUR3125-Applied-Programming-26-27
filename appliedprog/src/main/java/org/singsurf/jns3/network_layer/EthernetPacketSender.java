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
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;

/**
 * Sends IP packets over Ethernet. 
 * It uses ARP to resolve IP addresses to MAC addresses
 * and fragments packets if they are too large for the Ethernet frame.
 * Our implementation uses the low size of 140 bytes.
 */
public class EthernetPacketSender implements PacketSender, TickListener {
	AddressableDevice device;
	String name;
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
	
	public EthernetPacketSender(AddressableDevice device, 
			IPProtocol arpController) {
		this.device = device;
		this.name = device.getName();
		this.ipProtocol = arpController;
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void send(IPPacket packet) {
		
		var destIP = packet.getDestinationAddress();
		var srcIP = packet.getSourceAddress();
		var destMac = lookup(destIP);
		var srcMac = srcIP.equals(device.getIPAddress())
				? device.getMACAddress()
				: lookup(srcIP);
		
		if(destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS) 
		 || destIP.equals(device.getMulticastAddress())) {
			destMac = MACAddress.BROADCAST_MAC;
			if(srcMac == null) {
				Jns3Logger.logError(name, "No ARP entry for source IP " + srcIP + ". Cannot send multicast packet.");
				return;
			}
			sendPacket(packet, srcMac, destMac);
			return;
		}
		if(destMac == null) {
			// No ARP entry for destination IP
			var queue = packetQueue.computeIfAbsent(destIP,
					k -> new TaggedQueue(new LinkedList<>(),false));
			queue.queue().add(packet);
			if(!queue.requested()) {
				ipProtocol.sendArpRequest(destIP);
				queue.requested = true;
			}
			packetQueue.put(destIP, new TaggedQueue(queue.queue(), true));
			Jns3Logger.logInfo(name, "No ARP entry for " + destIP + ". Packet queud.");
			return;
		}
		
		sendPacket(packet, srcMac, destMac);
	}

	MACAddress lookup(IPAddress destIP) {
		MACAddress mac = ipProtocol.lookup(destIP);
		return mac;
	}

	private void sendPacket(IPPacket packet, MACAddress srcMac, MACAddress destMac) {
		List<IPPacket> packets = packetFragmentor.getFragments(packet);
		for(IPPacket p : packets) {
			EthernetFrame frame = new EthernetFrame(
					destMac, srcMac,
					IPProtocol.ETHERTYPE,
					p.getBytes());
			if(!IPPacketExtractor.getInstance().isExtractable(frame)) {
				Jns3Logger.logError(name, "Frame is not extractable as IP packet. Frame: " + frame);
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
			var destMac = lookup(destIp);
			if(destMac != null) {
				while(!queue.queue().isEmpty()) {
					IPPacket packet = queue.queue().poll();
					send(packet);
				}
				toRemove.add(destIp);
				Jns3Logger.logInfo(name, "Sent "+size+" queued packets to " + destIp + " after ARP resolution.");
			}
		}
		for(var ip: toRemove) {
			if(packetQueue.get(ip).queue().isEmpty())
				packetQueue.remove(ip);
		}
		for(var ip: packetQueue.keySet()) {
			ipProtocol.sendArpRequest(ip);
		}
		
	}

	public void setFragmentSize(int size) {
		packetFragmentor.setMaxLength(size);
		
	}

	public void setFrameSender(FrameSender frameSender2) {
		this.frameSender = frameSender2;		
	}

}
