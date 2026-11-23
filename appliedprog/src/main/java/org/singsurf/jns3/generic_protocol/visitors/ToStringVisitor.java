package org.singsurf.jns3.generic_protocol.visitors;

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
import org.singsurf.jns3.transport_layer.tcp.SocketMessage;
import org.singsurf.jns3.transport_layer.tcp.TCPProtocol;
import org.singsurf.jns3.transport_layer.tcp.TCPSegment;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * A visitor that generates a string representation of the protocol container and its children.
 * Each layer in the string is include on a separate line
 * indented to show the hierarchy of the protocol stack.
 * @since Week 18
 */
public class ToStringVisitor extends AbstractVisitor<String> {
	private static final String INDENT_STR = "  ";

	public ToStringVisitor(ProtocolDevice device) {
		super(device);
	}
	
	/**
	 * Main entry point for generating a string representation of a protocol container
	 * and its children.
	 * @param container the frame or packet or other type of message to generate a string representation for.
	 * @return a string representation of the container and its children
	 */
	public String toString(ProtocolContainer container) {
		return start(container);
	}
	
	String indent(String str) {
		String[] lines = str.split("\n");
		StringBuilder sb = new StringBuilder();
		for(String line: lines) {
			sb.append(INDENT_STR).append(line).append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * A template method that handles the common logic of visiting a protocol container and its children.
	 * <p>
	 * @param <T> The type of the protocol container.
	 * @param prot The protocol to visit.
	 * @param container The protocol container to process.
	 * @return a string representation of the protocol container and its children, or null if no children were found.
	 */
	private <T extends ProtocolContainer> String innerVisit(UpperPort<T> prot, T container) {
		StringBuilder sb = new StringBuilder();
		sb.append(container.toHeaderString()).append('\n');
		boolean foundChild = false;
		for(var child: prot.getChildProtocols()) {
			var res = child.accept(this, container);
			if(res != null) {
				sb.append(indent(res));
				foundChild = true;
			}
		}
		if(!foundChild) {
			sb.append(INDENT_STR).append(new String(container.getContents())).append('\n');
		}
		return sb.toString();
	}
	
	@Override
	public String visit(EthernetProtocol protocol, EthernetFrame frame) {
		return innerVisit(protocol, frame);
	}

	@Override
	public String visit(IPProtocol protocol, IPPacket packet) {
		return innerVisit(protocol, packet);
	}

	@Override
	public String visit(ARPProtocol protocol, ARPMessage msg) {
		return msg.toString();
	}

	@Override
	public String visit(ICMPProtocol protocol, ICMPMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg.toHeaderString()).append('\n');
		if(msg.getType() == ICMPMessage.TIME_EXCEEDED 
			|| msg.getType() == ICMPMessage.PARAMETER_PROBLEM 
			|| msg.getType() == ICMPMessage.DEST_UNREACHABLE_TYPE) {
			// Data contains the original IP header and the first 8 bytes of the original payload
			var ipHeader = msg.dataAsIPHeader();
			var inner = visit(device.getIPProtocol(), ipHeader);
			sb.append(indent(inner));
		} else if(msg.getContents().length > 0) {
			sb.append(INDENT_STR).append(new String(msg.getContents())).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String visit(UDPProtocol protocol, UDPDatagram datagram) {
		StringBuilder sb = new StringBuilder();
		sb.append(datagram.toHeaderString()).append('\n');
		if(datagram.getContents().length == 0) {
			return sb.toString();
		}
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, datagram);
			if(res != null) {
				sb.append(indent(res));
				return sb.toString();
			}
		}
		if(datagram.getContents().length > 0) {
			sb.append(INDENT_STR).append(new String(datagram.getContents())).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String visit(DHCPProtocol protocol, DHCPMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg.toHeaderString()).append('\n');
		var options = msg.getOptions();
		for(var option: options) {
			sb.append(INDENT_STR).append(option).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String visit(TCPProtocol protocol, TCPSegment segment) {
		StringBuilder sb = new StringBuilder();
		sb.append(segment.toHeaderString()).append('\n');
		if(segment.getContents().length == 0) {
			return sb.toString();
		}
		SocketMessage msg = new SocketMessage("tcp", 
				segment.getPsudoHeader().sourceIP(), segment.getPsudoHeader().destinationIP(), 
				segment.getSourcePort(), segment.getDestinationPort(), 
				segment.getContents());
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, msg);
			if(res != null) {
				sb.append(indent(res));
				return sb.toString();
			}
		}
		if(segment.getContents().length > 0) {
			sb.append(indent(new String(segment.getContents()))).append('\n');
		}
		return sb.toString();
	}

}
