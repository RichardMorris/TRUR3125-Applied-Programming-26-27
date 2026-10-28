package org.singsurf.jns3.network_layer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
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
	String name;
	MACAddress sourceMac;
	ARPController arpController;
	FrameSender frameSender;

	PacketFragmentor packetFragmentor = new PacketFragmentor(140);
	
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
			ARPController arpController, FrameSender frameSender) {
		this.name = device.getName();
		this.sourceMac = device.getMACAddress();
		this.arpController = arpController;
		this.frameSender = frameSender;
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void sendPacket(IPPacket packet) {
		
		var destIP = packet.getDestinationAddress();
		var destMac = arpController.getArpTable().get(destIP);
		
		if(destMac == null) {
			var queue = packetQueue.computeIfAbsent(destIP,
					k -> new TaggedQueue(new LinkedList<>(),false));
			queue.queue().add(packet);
			if(!queue.requested()) {
				arpController.sendArpRequest(destIP);
				queue.requested = true;
			}
			packetQueue.put(destIP, new TaggedQueue(queue.queue(), true));
			Jns3Logger.logInfo(name, "No ARP entry for " + destIP + ". Packet queud.");
			return;
		}
		
		sendPacket(packet, destMac);
	}

	private void sendPacket(IPPacket packet, MACAddress destMac) {
		List<IPPacket> packets = packetFragmentor.getFragments(packet);
		for(IPPacket p : packets) {
			EthernetFrame frame = new EthernetFrame(destMac, sourceMac, p.getBytes());
			if(!IPPacketExtractor.getInstance().isExtractable(frame)) {
				Jns3Logger.logError(name, "Frame is not extractable as IP packet. Frame: " + frame);
			}
			frameSender.sendFrame(frame);
		}
	}

	@Override
	public void onTick() {
		List<IPAddress> toRemove = new LinkedList<>();
		for(var ent: packetQueue.entrySet()) {
			var destIp = ent.getKey();
			var queue = ent.getValue();
			var size = queue.queue().size();
			var destMac = arpController.getArpTable().get(destIp);
			if(destMac != null) {
				while(!queue.queue().isEmpty()) {
					IPPacket packet = queue.queue().poll();
					sendPacket(packet,destMac);
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
			arpController.sendArpRequest(ip);
		}
		
	}

}
