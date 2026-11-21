package org.singsurf.jns3.transport_layer.udp;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.ProtocolVisitor;
import org.singsurf.jns3.generic_protocol.UpperSocketPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram.PseudoHeader;

/**
 * Implements the User Datagram Protocol.
 * Provides a socket factory for creating UDP sockets, 
 * and a controller for handling incoming UDP packets
 * directed them to the appropriate socket.
 */
public class UDPProtocol implements Protocol, LowerAdapter<IPPacket>, UpperSocketPort<UDPDatagram> {

	UDPController udpController;
	ProtocolDevice device;
	String name;
	UDPSocketFactory socketFactory;
	List<LowerSocketAdapter<UDPDatagram>> childProtocols = new ArrayList<>();
	
	public UDPProtocol(ProtocolDevice device) {
		this.device = device;
		this.name = device.getName() + "-UDP";
		udpController = new UDPController(device);
		socketFactory = new UDPSocketFactory(this);
	}

	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(udpController);
	}

	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		udpController.setPacketSender((PacketSender) sender);
	}

	@Override
	public void register(LowerSocketAdapter<UDPDatagram> adapter) {
		adapter.setSocketFactory(socketFactory);
		childProtocols.add(adapter);
	}

	public UDPSocketFactory getSocketFactory() {
		return socketFactory;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public <R> R accept(ProtocolVisitor<R> visitor, IPPacket packet) {
		if(packet.getProtocol() != IPPacket.UDP) return null;
		var psudo = new PseudoHeader(packet.getSourceAddress(), 
				packet.getDestinationAddress(), IPPacket.UDP,
				(short) packet.getContents().length);
		if(!UDPDatagram.isExtractable(packet.getContents()))
			return null;
		var datagram = UDPDatagram.unsafeFromBytes(packet.getContents(),psudo);
		return visitor.visit(this, datagram);
	}

	@Override
	public List<LowerSocketAdapter<UDPDatagram>> getChildProtocols() {
		return childProtocols;
	}


}
