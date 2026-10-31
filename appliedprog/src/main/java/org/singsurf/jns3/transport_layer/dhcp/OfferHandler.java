package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;

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
		Jns3Logger.logInfo(name, "Received DHCP Offer message YIADDR "+ message.getYiaddr().toString());
//		IPAddress currentIP=null;
//		for(var opt : message.options) {
//			if(opt.code() == DHCPMessage.OPT_REQ_IP) {
//				currentIP = opt.getDataAsIPAddress();
//			}
//		}
//		if(currentIP == null) {
//			Jns3Logger.logError("DiscoverHandler", "No requested IP address in DHCP Discover message");
//			return HandlerStatus.COMPLETED;
//		}
//		
//		var offerIP = DeviceFactory.getInstance().createSequentialLocalIPAddress();
//		DHCPMessage reply = DHCPMessage.createOfferMessage(
//				message.getMacAddress(), device.getIPAddress(), offerIP, message.getXid());
//		UDPDatagram datagram = new UDPDatagram(
//				DHCPProtocol.DHCP_SERVER_PORT, 
//				DHCPProtocol.DHCP_CLIENT_PORT,  
//				reply.getBytes());
//		// We need to open a socket 
//		// with src ip = 
//		dhcpProtocol.arpProtocol.put(currentIP, message.getMacAddress());
//		var sock = dhcpProtocol.factory.createSocket("DHCP Offer Socket");
//		sock.bind(offerIP, DHCPProtocol.DHCP_SERVER_PORT);
//		sock.send(datagram);
//		
////		dhcpProtocol.socket.send(datagram);
		return HandlerStatus.COMPLETED;
	}

}
