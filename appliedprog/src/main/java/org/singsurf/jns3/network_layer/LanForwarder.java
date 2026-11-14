package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.HandlerStatus;

/**
 * Forwards IP packets to devices on the LAN.
 * <p>
 * Uni-cast packets to the device's own IP address are passed on to the next handler.
 * Uni-cast packets to devices matching the subnet mask are sent and no further handlers
 * are used.
 * Local multi-cast packets to the device's multi-cast address or limited multi-cast
 * address forwarded and pass to the next handler.  
 */
public class LanForwarder implements PacketHandler {

	private IPProtocol protocol;

	public LanForwarder(IPProtocol protocol) {
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
