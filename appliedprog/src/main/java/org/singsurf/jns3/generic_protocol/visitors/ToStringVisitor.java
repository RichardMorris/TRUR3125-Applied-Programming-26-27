package org.singsurf.jns3.generic_protocol.visitors;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.ProtocolVisitor;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * A visitor that generates a string representation of the protocol container and its children.
 * Each layer in the string is include on a separate line
 * indented to show the hierarchy of the protocol stack.
 * @since week 18
 */
public class ToStringVisitor implements ProtocolVisitor<String> {

	public ToStringVisitor() {
	}

	String indent(String str) {
		String[] lines = str.split("\n");
		StringBuilder sb = new StringBuilder();
		for(String line: lines) {
			sb.append("  ").append(line).append('\n');
		}
		return sb.toString();
	}
	@Override
	public String visit(EthernetProtocol protocol, EthernetFrame frame) {
		StringBuilder sb = new StringBuilder();
		sb.append(frame.toHeaderString()).append('\n');
		boolean foundChild = false;
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, frame);
			if(res != null) {
				sb.append(indent(res));
				foundChild = true;
			}
		}
		if(!foundChild) {
			sb.append("  ").append(frame.getPayloadAsString()).append('\n');
		}
		
		return sb.toString();
	}

	@Override
	public String visit(IPProtocol protocol, IPPacket packet) {
		StringBuilder sb = new StringBuilder();
		sb.append(packet.toHeaderString()).append('\n');
		boolean foundChild = false;
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, packet);
			if(res != null) {
				sb.append(indent(res));
				foundChild = true;
			}
		}
		if(!foundChild) {
			sb.append("  ").append(packet.getDataAsString()).append('\n');
		}		
		return sb.toString();
	}

	@Override
	public String visit(ARPProtocol protocol, ARPMessage msg) {
		return msg.toString();
	}

	@Override
	public String visit(ICMPProtocol protocol, ICMPMessage msg) {
		return msg.toString() + "\n";
	}

	@Override
	public String visit(UDPProtocol protocol, UDPDatagram datagram) {
		StringBuilder sb = new StringBuilder();
		sb.append(datagram.toHeaderString()).append('\n');
		boolean foundChild = false;
		for(var child: protocol.getChildProtocols()) {
			var res = child.accept(this, datagram);
			if(res != null) {
				sb.append(indent(res));
				foundChild = true;
			}
		}
		if(!foundChild) {
			sb.append("  ").append(new String(datagram.getContents())).append('\n');
		}		
		return sb.toString();
	}

	@Override
	public String visit(DHCPProtocol protocol, DHCPMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg.toHeaderString()).append('\n');
		var options = msg.getOptions();
		for(var option: options) {
			sb.append("  ").append(option).append('\n');
		}
		return sb.toString();
	}

}
