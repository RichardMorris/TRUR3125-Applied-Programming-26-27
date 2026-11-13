package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.HandlerStatus;

public class IPPacketForwarder implements PacketHandler {

	private IPProtocol protocol;

	public IPPacketForwarder(IPProtocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		var destIP = packet.getDestinationAddress();
		if(destIP.equals(protocol.device.getIPAddress())) {
			return HandlerStatus.CONTINUE;
		}
		if(protocol.device.getSubnetMask().matches(
				protocol.device.getIPAddress(), destIP)) {
			protocol.getPacketSender().send(packet);
			
			if(destIP.equals(protocol.device.getMulticastAddress()) 
					|| destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS)) {
					return HandlerStatus.CONTINUE;
			} else {
					return HandlerStatus.COMPLETED;
			}
		}
		return HandlerStatus.CONTINUE;
	}

}
