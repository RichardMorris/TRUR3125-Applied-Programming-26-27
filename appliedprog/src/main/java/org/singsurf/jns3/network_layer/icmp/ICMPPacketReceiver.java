package org.singsurf.jns3.network_layer.icmp;

import java.util.List;

import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketHandler;

/** The receiver for ICMP packets,
 * it delegates messages to the appropriate handler.
 */
public class ICMPPacketReceiver implements PacketHandler {
	String name;
	
	ProtocolDelegator<ICMPMessage> delegator;
	
	public ICMPPacketReceiver(String name) {
		this.name = name;
		delegator = new ProtocolDelegator<>(name);
	}
	
	public void addHandler(ICMPMessageHandler handler) {
		delegator.addHandler(handler);			
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		if(packet.getProtocol() != 1) {
			return HandlerStatus.CONTINUE;
		}
		// ICMP protocol number is 1
		ICMPMessage message = ICMPPacketExtractor.getInstance().extract(packet);
		delegator.receive(message);
		return HandlerStatus.COMPLETED;
	}
	
	public List<ProtocolHandler<ICMPMessage>> getHandlers() {
		return delegator.getHandlers();
	}
}
