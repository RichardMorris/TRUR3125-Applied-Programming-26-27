package org.singsurf.jns3.network_layer.icmp;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketReceiver;

/** The receiver for ICMP packets,
 * it delegates messages to the appropriate handler.
 */
public class ICMPPacketReceiver implements PacketReceiver {
	PacketReceiver nextReceiver;
	String name;
	List<ICMPMessageHandler> handlers = new ArrayList<>();
	
	public ICMPPacketReceiver(String name,PacketReceiver receiver) {
		this.name = name;
		this.nextReceiver = receiver;
	}
	
	@Override
	public void receivePacket(IPPacket packet) {
		if(packet.getProtocol() != 1) {
			nextReceiver.receivePacket(packet);
			return;
		}
		// ICMP protocol number is 1
		ICMPMessage message = ICMPPacketExtractor.getInstance().extract(packet);
		for (ICMPMessageHandler handler : handlers) {
			if (handler.matches(message)) {
				handler.handle(packet, message);
				return;
			}
		}
		Jns3Logger.logError(name, "Received unhandled ICMP packet: " + packet);
			
	}

	public void addHandler(ICMPMessageHandler handler) {
		handlers.add(handler);
	}
}
