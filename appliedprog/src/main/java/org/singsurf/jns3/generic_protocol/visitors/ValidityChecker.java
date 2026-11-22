package org.singsurf.jns3.generic_protocol.visitors;

import java.util.Arrays;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.tcp.InternetMessage;
import org.singsurf.jns3.transport_layer.tcp.TCPProtocol;
import org.singsurf.jns3.transport_layer.tcp.TCPSegment;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * A visitor that checks the validity of protocol containers
 * and its encapsulated containers.
 * 
 * It can check at different levels: DATA_LINK, NETWORK, TRANSPORT, or ALL.
 * The visitor returns true if the container is valid, false otherwise.
 */
public class ValidityChecker extends AbstractVisitor<Boolean> {
	public enum Level {
		DATA_LINK, NETWORK, TRANSPORT, ALL
	}
	Level level = Level.ALL;
	
	public ValidityChecker(ProtocolDevice device) {
		super(device);
	}

	/**
	 * Checks the validity of the given protocol container and its encapsulated containers.
	 * @param container the protocol container to be checked
	 * @return true if the container is valid, false otherwise
	 */
	public boolean check(ProtocolContainer container) {
		level = Level.ALL;
		return start(container);
	}

	/**
	 * Checks the validity of the given protocol container and its encapsulated containers
	 * up to the specified level.
	 * For example if there is a UDP datagram inside an IP packet inside an Ethernet frame, 
	 * and the level is set to NETWORK, then
	 * the validity of the Ethernet frame and the IP packet will be checked, but not the UDP datagram.
	 * @param container the protocol container to be checked
	 * @param lvl the level up to which to check (DATA_LINK, NETWORK, TRANSPORT, or ALL)
	 * @return true if the container is valid, false otherwise
	 */
	public boolean check(ProtocolContainer container, Level lvl) {
		level = lvl;
		return start(container);
	}

	private <T extends ProtocolContainer> Boolean innerVisit(UpperPort<T> prot, T container) {
		for(var child: prot.getChildProtocols()) {
			var res = child.accept(this, container);
			if(res != null) {
				return res;
			}
		}
		return true; // no child protocols, so the container is well-formed
	}

	@Override
	public Boolean visit(EthernetProtocol protocol, EthernetFrame frame) {
		if(!frame.isValid())
			return false;
		if(level == Level.DATA_LINK) return true;
		return innerVisit(protocol, frame);
	}

	@Override
	public Boolean visit(IPProtocol protocol, IPPacket packet) {
		if(!packet.isValid())
			return false;
		if(level == Level.NETWORK) return true;
		return innerVisit(protocol, packet);
	}

	@Override
	public Boolean visit(ARPProtocol protocol, ARPMessage message) {
		return message.isValid();
	}

	@Override
	public Boolean visit(ICMPProtocol protocol, ICMPMessage message) {
		return message.isValid();
	}

	@Override
	public Boolean visit(UDPProtocol protocol, UDPDatagram datagram) {
		if(!datagram.isValid()) {
			return false;
		}
		if(level == Level.TRANSPORT) return true;
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, datagram);
			if(res != null) {
				return res;
			}
		}
		return true; // no child protocols, so the datagram is well-formed
	}

	@Override
	public Boolean visit(DHCPProtocol protocol, DHCPMessage message) {
		return message.isValid();
	}

	@Override
	public Boolean visit(TCPProtocol protocol, TCPSegment segment) {
		System.out.println(Arrays.toString(segment.getBytes()));
		System.out.println(segment.toString());
		System.out.println(segment.getPsudoHeader());
		System.out.println(segment.calculateHeaderChecksum());

		if(!segment.isValid()) {
			return false;
		}
		if(level == Level.TRANSPORT) return true;
		
		InternetMessage msg = new InternetMessage("tcp", 
				segment.getPsudoHeader().sourceIP(), segment.getPsudoHeader().destinationIP(), 
				segment.getSourcePort(), segment.getDestinationPort(), 
				segment.getContents());

		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, msg);
			if(res != null) {
				return res;
			}
		}
		return true; // no child protocols, so the datagram is well-formed
	}
}
