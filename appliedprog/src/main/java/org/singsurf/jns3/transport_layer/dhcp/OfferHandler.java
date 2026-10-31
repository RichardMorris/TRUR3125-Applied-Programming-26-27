package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class OfferHandler implements ProtocolHandler<DHCPMessage> {
	DHCPProtocol dhcpProtocol;
	AddressableDevice device;
	String name;
	
	public OfferHandler(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
		this.device = dhcpProtocol.device;
		this.name = dhcpProtocol.device.getName() + "-DHCP-Offer";
	}
	
	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPProtocol.DHCP_OP_OFFER) {
			return HandlerStatus.CONTINUE;
		}
		
		var op = message.getOption(DHCPMessage.OPT_MSG_TYPE);
		if(op == null || op.data()[0] != DHCPProtocol.DHCP_OP_OFFER) {
			return HandlerStatus.CONTINUE;
		}

		dhcpProtocol.lastOfferMessage = message;
		Jns3Logger.logInfo(name, "Received DHCP Offer message YIADDR "+ message.getYiaddr().toString());
		MACAddress mac = message.getOption(DHCPMessage.OPT_SERVER_MAC).getDataAsMACAddress();
		if(mac == null) {
			Jns3Logger.logError(name, "No server MAC address in DHCP Offer message");
			return HandlerStatus.COMPLETED;
		}
		dhcpProtocol.arpProtocol.put(message.siaddr, mac);

//		// We need to open a socket 
//		// with src ip = 
//		var sock = dhcpProtocol.factory.createSocket("DHCP Offer Socket");
//		sock.bind(message.siaddr, DHCPProtocol.DHCP_CLIENT_PORT);

		DHCPMessage reply = DHCPMessage.createRequestMessage(
				message.getMacAddress(),
				message.yiaddr,
				message.siaddr,
				message.getXid());
		
		UDPDatagram datagram = new UDPDatagram(
				DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT,  
				reply.getBytes());
		dhcpProtocol.multicast_socket.send(datagram);
		return HandlerStatus.COMPLETED;
	}

}
