package org.singsurf.jns3.generic_protocol.visitors;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.generic_protocol.ProtocolVisitor;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

/**
 * Abstract visitor class that implements the ProtocolVisitor interface and provides a start method to initiate the visitor pattern.
 * @param <R> return type of the visit methods
 */
public abstract class AbstractVisitor<R> implements ProtocolVisitor<R> {

	protected ProtocolDevice device;

	/**
	 * Constructor for AbstractVisitor.
	 * @param device used to find the correct protocol for the data type being visited.
	 */
	public AbstractVisitor(ProtocolDevice device) {
		this.device = device;
	}
	/**
	 * Start the visitor by finding the correct protocol for 
	 * the data type andd calling the correct visit method.
	 * Uses <a href="https://openjdk.org/jeps/441">JEP 441: Pattern Matching for switch</a> 
	 * to determine the type of the container and dispatch to the appropriate visit method.
	 * @param container the frame or packet or other type of message to generate a string representation for.
	 * @return a string representation of the container and its children
	 */
	protected R start(ProtocolContainer container) {
		switch(container) {
			case EthernetFrame frame -> {
				return visit(device.getEthernetProtocol(), frame);
			}
			case IPPacket packet -> {
				return visit(device.getIPProtocol(), packet);
			}
			case ARPMessage msg -> {
				return visit(device.getArpProtocol(), msg);
			}
			case ICMPMessage msg -> {
				return visit(device.getICMPProtocol(), msg);
			}
			case UDPDatagram datagram -> {
				return visit(device.getUDPProtocol(), datagram);
			}
			case DHCPMessage msg -> {
				return visit(device.getDHCPProtocol(), msg);
			}
		default -> throw new IllegalArgumentException("Unexpected value: " + container);
		}
		
	}

}