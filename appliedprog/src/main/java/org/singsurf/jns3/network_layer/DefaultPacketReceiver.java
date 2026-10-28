package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogLevel;
import org.singsurf.jns3.generic_protocol.HandlerStatus;

/**
 * Default implementation of PacketReceiver that stores received packets in a list
 * and logs the received packets using Jns3Logger.
 */
public class DefaultPacketReceiver implements PacketHandler {
	String name;
	List<IPPacket> recivedPackets = new ArrayList<>();
	LogLevel logLevel = LogLevel.RECEIVED;
	
	public DefaultPacketReceiver(String name) {
		this.name = name;
	}
	
	public DefaultPacketReceiver(String name, LogLevel logLevel) {
		this.name = name;
		this.logLevel = logLevel;
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

	@Override
	public HandlerStatus handle(IPPacket packet) {
		recivedPackets.add(packet);
		Jns3Logger.log(name, logLevel, "Received packet: " + packet);
		return HandlerStatus.COMPLETED;
	}
	

}
