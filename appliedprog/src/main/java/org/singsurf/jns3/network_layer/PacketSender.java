package org.singsurf.jns3.network_layer;

/**
 * Defines a sender of IP packets.
 */
public interface PacketSender {
	
	void sendPacket(IPPacket packet);
}
