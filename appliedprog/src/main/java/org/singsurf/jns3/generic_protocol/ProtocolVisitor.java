package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * A Generic visitor interface for protocol data.
 * Implementing classes can use the
 * {@link UpperPort#getChildProtocols()} method to get the child protocols
 * of this protocol and then call the
 * {@link LowerAdapter#accept(ProtocolVisitor, ProtocolContainer)} method 
 * on the child protocol to recurse down the tree.
 * @param <R> Return data type.
 * @since week 18
 */
public interface ProtocolVisitor<R> {

	R visit(EthernetProtocol protocol, EthernetFrame frame);
	R visit(IPProtocol protocol, IPPacket frame);
	R visit(ARPProtocol protocol, ARPMessage frame);
	R visit(ICMPProtocol protocol, ICMPMessage frame);
	R visit(UDPProtocol protocol, UDPDatagram frame);
	R visit(DHCPProtocol protocol, DHCPMessage frame);

}
