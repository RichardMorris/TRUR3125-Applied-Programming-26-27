package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.generic_protocol.ProtocolReceiver;

/**
 * Defines a receiver of IP packets.
 */
public interface PacketReceiver extends ProtocolReceiver<IPPacket> {
	
	void receive(IPPacket packet);
}
