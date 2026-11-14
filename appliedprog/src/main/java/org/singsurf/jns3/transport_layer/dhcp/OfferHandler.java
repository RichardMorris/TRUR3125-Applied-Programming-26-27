package org.singsurf.jns3.transport_layer.dhcp;

import java.util.ArrayList;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.SubnetMask;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

/**
 * Handles incoming DHCP Offer messages and stores them in the protocol for later processing.
 * The client should call {@link #acceptOffer(DHCPMessage)} to accept an offer 
 * and send a DHCP Request message to the server.
 */
public class OfferHandler implements ProtocolHandler<DHCPMessage> {
	DHCPProtocol dhcpProtocol;
	ProtocolDevice device;
	String name;
	
	public OfferHandler(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
		this.device = dhcpProtocol.device;
		this.name = dhcpProtocol.device.getName() + "-DHCP-Offer";
	}
	
	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPMessage.DHCP_OP_OFFER) {
			return HandlerStatus.CONTINUE;
		}
		
		var op = message.getOption(DHCPMessage.OPT_MSG_TYPE);
		if(op == null || op.data()[0] != DHCPMessage.DHCP_OP_OFFER) {
			return HandlerStatus.CONTINUE;
		}


		Jns3Logger.logInfo(name, "Received DHCP Offer message YIADDR "+ message.getYiaddr().toString());
		MACAddress mac = message.getOption(DHCPMessage.OPT_SERVER_MAC).getDataAsMACAddress();
		if(mac == null) {
			return HandlerStatus.invalid("No server MAC address in DHCP Offer message");
		}
		device.getArpProtocol().put(message.siaddr, mac);

		var offers = dhcpProtocol.offerMessages.computeIfAbsent(message.getXid(), k ->
			new ArrayList<>());
		offers.add(message);
		
	    // Second option: 0x0104ffffff00: Option 1 (Subnet mask) 4 octets (containing 255.255.255.0)
		//		options.add(new DHCPOption(OPT_SUBNET_MASK, DHCPProtocol.SUBNET_MASK));

		var subop = message.getOption(DHCPMessage.OPT_SUBNET_MASK);
		if(subop != null) {
			var maskIP = subop.getDataAsIPAddress();
			var subnetMask = new SubnetMask(maskIP);
			dhcpProtocol.device.setSubnetMask(subnetMask);
		} else {
			Jns3Logger.logError(name, "No subnet mask in DHCP Offer message");
		}
		return HandlerStatus.COMPLETED;
	}
	
	/**
	 * Accepts the DHCP Offer message and sends a DHCP Request message to the server.
	 * @param message The DHCP Offer message to accept.
	 */
	public void acceptOffer(DHCPMessage message) {
		Jns3Logger.logInfo(name, "Sending DCHP Request YIADDR "+ message.getYiaddr().toString());
		DHCPMessage reply = DHCPMessage.createRequestMessage(message);
		
		UDPDatagram datagram = new UDPDatagram(
				DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT,  
				reply.getBytes());
		dhcpProtocol.multicast_socket.send(datagram);
	}

}
