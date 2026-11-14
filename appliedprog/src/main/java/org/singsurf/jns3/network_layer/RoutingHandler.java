package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.ProtocolDevice;

/**
 * A handler for routing in-bound IP packets from the internet.
 * <p>
 * Packets are modified to use the private IP address corresponding to the destination's public IP.
 * Packets with a local destination IP address are ignored.
 */
public class RoutingHandler implements ProtocolHandler<IPPacket> {
	RoutingProtocol routingProtocol;
	ProtocolDevice device;
	public RoutingHandler(RoutingProtocol routingProtocol) {
		this.routingProtocol = routingProtocol;
		this.device = routingProtocol.protocolDevice;
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		var destIP = packet.getDestinationAddress();
		if(device.isLocalAddress(destIP)
			|| destIP.equals(device.getMulticastAddress()) 
			|| destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS)) {
					return HandlerStatus.CONTINUE;
		}
		var privateIP = routingProtocol.getPrivateIP(destIP);
		if(privateIP != null) {
			packet.setDestinationIP(privateIP);
		}
		
		routingProtocol.getPacketSender().send(packet);

		return HandlerStatus.COMPLETED;
	}

}
