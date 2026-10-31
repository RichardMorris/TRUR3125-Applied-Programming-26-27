package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class DiscoverHandler implements ProtocolHandler<DHCPMessage> {
	DHCPProtocol dhcpProtocol;
	AddressableDevice device;
	String name;
	public DiscoverHandler(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
		this.device = dhcpProtocol.device;
		this.name = dhcpProtocol.device.getName() + "-DHCP-Discover";
	}
	
	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPMessage.DHCP_OP_DISCOVER
				|| !message.siaddr.equals(IPAddress.ZERO_ADDRESS)) {
			return HandlerStatus.CONTINUE;
		}
		Jns3Logger.logInfo(name, "Received DHCP Discover message");
		IPAddress currentIP=message.getOption(DHCPMessage.OPT_REQ_IP).getDataAsIPAddress();
		if(currentIP == null) {
			Jns3Logger.logError(name, "No requested IP address in DHCP Discover message");
			return HandlerStatus.COMPLETED;
		}
		
		var offerIP = DeviceFactory.getInstance().createSequentialLocalIPAddress();
		
		DHCPMessage reply = DHCPMessage.createOfferMessage(
				device.getMACAddress(), 
				device.getIPAddress(), 
				offerIP,
				message);
		UDPDatagram datagram = new UDPDatagram(
				DHCPProtocol.DHCP_SERVER_PORT, 
				DHCPProtocol.DHCP_CLIENT_PORT,  
				reply.getBytes());
		// We need to open a socket 
		// with src ip = 
		dhcpProtocol.arpProtocol.put(currentIP, message.getMacAddress());
		var sock = dhcpProtocol.factory.createSocket("DHCP Offer Socket");
		sock.bind(currentIP, DHCPProtocol.DHCP_SERVER_PORT);
		dhcpProtocol.offerSockets.put(message.getXid(), (DatagramSocket) sock);
		
		sock.send(datagram);
		return HandlerStatus.COMPLETED;
	}

}
