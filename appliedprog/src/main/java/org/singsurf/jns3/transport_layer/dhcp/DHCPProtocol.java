package org.singsurf.jns3.transport_layer.dhcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.SocketFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class DHCPProtocol implements Protocol, LowerSocketAdapter<UDPDatagram>, ProtocolReceiver<UDPDatagram>, TickListener {
	public static final short DHCP_SERVER_PORT = 67; 
	public static final short DHCP_CLIENT_PORT = 68;
		
	public static final IPAddress  SUBNET_MASK  = new IPAddress("255.255.255.0");

	boolean isServer;
	ProtocolDevice device;

	DatagramSocket multicast_socket;	
	SocketFactory<UDPDatagram> factory;
	ProtocolDelegator<DHCPMessage> delgator;
	ARPProtocol arpProtocol;
	
	/** Sockets used by server to send offers/acknowledgements to clients.
	 *  Key is the xid the messages. 
	 **/
	Map<Integer, DatagramSocket> offerSockets = new HashMap<>();
	/**
	 * Offer Messages received by client from servers. 
	 * Key is the xid of the message. 
	 * Value is a list of offer messages received from different servers
	 * with that xid.
	 */
	Map<Integer, List<DHCPMessage>> offerMessages = new HashMap<>();
	/**
	 * Accepted Offer message
	 */
	Map<Integer, DHCPMessage> acceptedOfferMessages = new HashMap<>();
	
	private OfferHandler offerHandler;

	/**
	 * Create a new DHCPProtocol instance.
	 * @param device host device
	 * @param isServer true if this is a server, false if it is a client
	 */
	public DHCPProtocol(ProtocolDevice device,boolean isServer) {
		this.isServer = isServer;
		this.device = device;
		delgator = new ProtocolDelegator<DHCPMessage>(device.getName());
		if(isServer) {
			delgator.addHandler(new DiscoverHandler(this));
			delgator.addHandler(new RequestHandler(this));
		} else {
			offerHandler = new OfferHandler(this);
			delgator.addHandler(offerHandler);
			delgator.addHandler(new AckHandler(this));
		}
		GlobalWallClock.getInstance().addTickListener(this);
		this.arpProtocol = device.getArpProtocol();
	}


	@Override
	public void setSocketFactory(SocketFactory<UDPDatagram> factory) {
		this.factory = factory;
		multicast_socket = (DatagramSocket) factory.createSocket("DHCP Socket", this);
		multicast_socket.bind(IPProtocol.LIMITED_MULTICAST_ADDRESS,
				(short) (isServer ? DHCP_SERVER_PORT : DHCP_CLIENT_PORT));
		var nullSocket = (DatagramSocket) factory.createSocket("DHCP Null Socket", new ProtocolReceiver<UDPDatagram>() {

			@Override
			public void receive(UDPDatagram container) {
				Jns3Logger.logInfo(device.getName()+"-DHCP", "Null socket received message from " + container.getPsudoHeader().sourceIP() + ":" + container.getSourcePort());	
			}} );
		nullSocket.bind(IPProtocol.LIMITED_MULTICAST_ADDRESS, 
				(short) (isServer ? DHCP_CLIENT_PORT : DHCP_SERVER_PORT));
	}

	/**
	 *	Every tick checks to see if there has been an offer message.
	 *  If so accept the first one, 
	 **/

	@Override
	public void onTick() {
		for(var ent: offerMessages.entrySet()) {
			var xid = ent.getKey();
			var messages = ent.getValue();
			if(messages.size() >= 1) {
				var offer = messages.getFirst();
				messages.clear();
				acceptedOfferMessages.put(xid, offer);
				offerHandler.acceptOffer(offer);
			}
		}
	}

	public void startDiscovery() {
		DHCPMessage message = DHCPMessage.createDiscoverMessage(device.getMACAddress(), device.getIPAddress());
		UDPDatagram datagram = new UDPDatagram(DHCP_CLIENT_PORT, DHCP_SERVER_PORT, message.getBytes());
		Jns3Logger.logInfo(device.getName()+"-DHCP", "Sending DCHP Discover current IP "+ device.getIPAddress().toString());
		multicast_socket.send(datagram);
	}

	
	@Override
	public void receive(UDPDatagram msg) {
		DHCPMessage dhcpMessage = new DHCPMessage(msg.getContents());
		delgator.receive(dhcpMessage);
	}


	public Map<Integer, DHCPMessage> getAcceptedOfferMessages() {
		return acceptedOfferMessages;
	}


	public void announceAddress() {
		arpProtocol.announceAddress(device.getIPAddress(), device.getMACAddress());
		
	}

	void updateARP(IPAddress yiaddr, MACAddress macAddress) {
		arpProtocol.put(yiaddr, macAddress);
	}


	public void allocatePublicIP(IPAddress iaddr) {
		
		var routingProtocol = device.getRoutingProtocol();
		if(routingProtocol != null) {
			routingProtocol.allocatePublicIP(iaddr);
		}		
	}
}
