package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;

/**
 * Default implementation of PacketReceiver that stores received packets in a list
 * and logs the received packets using Jns3Logger.
 */
public class DefaultPacketReceiver implements PacketReceiver {
	String device;
	List<IPPacket> recivedPackets = new ArrayList<>();

	public DefaultPacketReceiver(String name) {
		this.device = name;
	}
	
	@Override
	public void receivePacket(IPPacket packet) {
		recivedPackets.add(packet);
		Jns3Logger.log(device, Jns3Logger.LogLevel.RECEIVED, "Received packet: " + packet);
	}
	
	public List<IPPacket> getReceivedPackets() {
		return recivedPackets;
	}
	
	public void clear() {
		recivedPackets.clear();
	}

	public int size() {
		return recivedPackets.size();
	}
	
	public IPPacket get(int index) {
		return recivedPackets.get(index);
	}
	

}
