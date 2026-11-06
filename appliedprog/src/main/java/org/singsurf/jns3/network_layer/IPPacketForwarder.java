package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.HandlerStatus;

public class IPPacketForwarder implements PacketHandler {

	private IPProtocol protocol;

	public IPPacketForwarder(IPProtocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		if(packet.getDestinationAddress().equals(protocol.device.getIPAddress())) {
			return HandlerStatus.CONTINUE;
		}
		protocol.getPacketSender().send(packet);

		if(packet.getDestinationAddress().equals(protocol.device.getMulticastAddress()) 
			|| packet.getDestinationAddress().equals(IPProtocol.LIMITED_MULTICAST_ADDRESS)) {
			return HandlerStatus.CONTINUE;
		} else {
			return HandlerStatus.COMPLETED;
		}

	}

}
