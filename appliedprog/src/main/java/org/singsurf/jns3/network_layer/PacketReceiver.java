package org.singsurf.jns3.network_layer;

/**
 * Defines a receiver of IP packets.
 */
public interface PacketReceiver {
	
	void receivePacket(IPPacket packet);
}
