package org.singsurf.jns3.network_layer.icmp;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketReceiver;

/** The receiver for ICMP packets,
 * it delegates messages to the appropriate handler.
 */
public class ICMPPacketReceiver  implements PacketReceiver {
	PacketReceiver nextReceiver;
	String name;
	List<ICMPMessageHandler> handlers = new ArrayList<>();
	
	ProtocolDelegator<ICMPMessage> delegator;
	
	public ICMPPacketReceiver(String name,PacketReceiver receiver) {
		this.name = name;
		this.nextReceiver = receiver;
		delegator = new ProtocolDelegator<>(name);
	}
	
	@Override
	public void receive(IPPacket packet) {
		if(packet.getProtocol() != 1) {
			nextReceiver.receive(packet);
			return;
		}
		// ICMP protocol number is 1
		ICMPMessage message = ICMPPacketExtractor.getInstance().extract(packet);
		delegator.receive(message);
		return;
	}

	public void addHandler(ICMPMessageHandler handler) {
		delegator.addHandler(handler);			
	}
}
