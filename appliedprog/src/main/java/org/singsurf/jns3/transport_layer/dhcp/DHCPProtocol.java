package org.singsurf.jns3.transport_layer.dhcp;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.generic_protocol.SocketFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class DHCPProtocol implements Protocol, LowerSocketAdapter<UDPDatagram>, TickListener {
	public static final short DHCP_SERVER_PORT = 67;
	public static final short DHCP_CLIENT_PORT = 68;
	
	public static final byte DHCP_PROTOCOL_TYPE = 0x11; // UDP
	
	public static final IPAddress  SUBNET_MASK  = new IPAddress("255.255.255.0");
	SocketFactory<UDPDatagram> factory;
	boolean isServer;
	ProtocolDevice device;
	ProtocolSocket<UDPDatagram> multicast_socket;
	
	
	ProtocolDelegator<DHCPMessage> delgator;
	ARPProtocol arpProtocol;
	DHCPMessage lastOfferMessage;
	Map<Integer, DatagramSocket> offerSockets = new HashMap<>();
	
	public DHCPProtocol(ProtocolDevice device,boolean isServer) {
		this.isServer = isServer;
		this.device = device;
		delgator = new ProtocolDelegator<DHCPMessage>(device.getName());
		if(isServer) {
			delgator.addHandler(new DiscoverHandler(this));
			delgator.addHandler(new RequestHandler(this));
		} else {
			delgator.addHandler(new OfferHandler(this));
			delgator.addHandler(new AckHandler(this));
		}
		GlobalWallClock.getInstance().addTickListener(this);
		this.arpProtocol = device.getProtocol(ARPProtocol.class);
	}


	@Override
	public void setSocketFactory(SocketFactory<UDPDatagram> factory) {
		this.factory = factory;
		multicast_socket = factory.createSocket("DHCP Socket");
		multicast_socket.bind(IPProtocol.LIMITED_MULTICAST_ADDRESS,
				(short) (isServer ? DHCP_SERVER_PORT : DHCP_CLIENT_PORT));
	}


	@Override
	public void onTick() {
		UDPDatagram msg;
		while((msg = multicast_socket.poll()) != null) {
			DHCPMessage dhcpMessage = new DHCPMessage(msg.getContents());
			delgator.receive(dhcpMessage);
		}
	}

	public void startDiscovery() {
		DHCPMessage message = DHCPMessage.createDiscoverMessage(device.getMACAddress(), device.getIPAddress());
		UDPDatagram datagram = new UDPDatagram(DHCP_CLIENT_PORT, DHCP_SERVER_PORT, message.getBytes());
		multicast_socket.send(datagram);
	}
}
