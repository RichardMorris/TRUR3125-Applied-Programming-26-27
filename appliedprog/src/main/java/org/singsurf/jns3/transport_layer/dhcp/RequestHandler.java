package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class RequestHandler implements ProtocolHandler<DHCPMessage> {
	DHCPProtocol dhcpProtocol;
	AddressableDevice device;
	String name;
	public RequestHandler(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
		this.device = dhcpProtocol.device;
		this.name = dhcpProtocol.device.getName() + "-DHCP-Request";
	}
	
	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPMessage.DHCP_OP_DISCOVER 
				|| message.siaddr.equals(IPAddress.ZERO_ADDRESS)) {
			return HandlerStatus.CONTINUE;
		}		
		if(!device.getIPAddress().equals(message.getSiaddr())) {
			Jns3Logger.logInfo(name, "Requested IP address " + message.getSiaddr() + " does not match device IP address " + device.getIPAddress());
			return HandlerStatus.COMPLETED;
		}
		
		Jns3Logger.logInfo(name, "Received DHCP request message");

		DHCPMessage reply = DHCPMessage.createAckMessage(
				device.getMACAddress(), message);

		UDPDatagram datagram = new UDPDatagram(
				DHCPProtocol.DHCP_SERVER_PORT, 
				DHCPProtocol.DHCP_CLIENT_PORT,  
				reply.getBytes());

		// Send to socket that was created for this XID in the DiscoverHandler
		var sock = dhcpProtocol.offerSockets.get(message.getXid());
		if(sock == null) {
			Jns3Logger.logError(name, "No socket found for DHCP request with XID " + message.getXid());
			return HandlerStatus.COMPLETED;
		}
		Jns3Logger.logInfo(name, "Sending DCHP Acknowledge YIADDR "+ message.getYiaddr().toString());

		sock.send(datagram);
		sock.close();
		dhcpProtocol.offerSockets.remove(message.getXid());
		
		// Update ARP table with the mapping of the offered IP address to the client's MAC address
		dhcpProtocol.arpProtocol.put(message.yiaddr, message.getMacAddress());

		return HandlerStatus.COMPLETED;
	}

}
