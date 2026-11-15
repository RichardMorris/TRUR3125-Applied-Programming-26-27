package org.singsurf.jns3.network_layer.routing;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.RoutingDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;

/**
 * A handler for routing in-bound IP packets from the internet.
 * <p>
 * Packets are modified to use the private IP address corresponding to the destination's public IP.
 * Packets with a local destination IP address are ignored.
 */
public class RoutingHandler implements ProtocolHandler<IPPacket> {
	RoutingProtocol routingProtocol;
	RoutingDevice device;
	public PacketSender packetSender;
	
	public RoutingHandler(RoutingProtocol routingProtocol) {
		this.routingProtocol = routingProtocol;
		this.device = routingProtocol.device;
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		var destIP = packet.getDestinationAddress();
		if(device.isLocalAddress(destIP)
			|| destIP.equals(device.getIPAddress())
			|| destIP.equals(device.getMulticastAddress()) 
			|| destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS)) {
					return HandlerStatus.CONTINUE;
		}
		
		if(device.getPrefix().matches(destIP)) {
			var privateIP = routingProtocol.getPrivateIP(destIP);
			if(privateIP != null) {
				packet.setDestinationIP(privateIP);
			} else {
				var srcMac = routingProtocol.lookup(packet.getSourceAddress());
				var destMac = routingProtocol.lookup(destIP);
				if(destMac == null || srcMac.equals(destMac)) {
					
					var errorMsg = ICMPMessage.createHostUnreachable(packet);
					var errorPacket = new IPPacket(
						IPPacket.IMCP, 64,
						device.getIPAddress(), packet.getSourceAddress(), 
						errorMsg.getBytes());
					Jns3Logger.logWarn(device.getName(), "Host unreachable "+packet);
					
					packetSender.send(errorPacket);
					return HandlerStatus.COMPLETED;
				}
			}
		}
		packetSender.send(packet);

		return HandlerStatus.COMPLETED;
	}

}
