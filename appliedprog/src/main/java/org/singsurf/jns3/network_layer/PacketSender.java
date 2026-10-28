package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.ProtocolSender;

/**
 * Defines a sender of IP packets.
 */
public interface PacketSender extends ProtocolSender<IPPacket> {
	
	void send(IPPacket packet);
}
