package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogLevel;

/**
 * Default implementation of PacketReceiver that stores received packets in a list
 * and logs the received packets using Jns3Logger.
 */
public class DefaultPacketReceiver implements PacketReceiver {
	String device;
	List<IPPacket> recivedPackets = new ArrayList<>();
	LogLevel logLevel = LogLevel.RECEIVED;
	
	public DefaultPacketReceiver(String name) {
		this.device = name;
	}
	
	public DefaultPacketReceiver(String name, LogLevel logLevel) {
		this.device = name;
		this.logLevel = logLevel;
	}
	
	@Override
	public void receive(IPPacket packet) {
		recivedPackets.add(packet);
		Jns3Logger.log(device, logLevel, "Received packet: " + packet);
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
