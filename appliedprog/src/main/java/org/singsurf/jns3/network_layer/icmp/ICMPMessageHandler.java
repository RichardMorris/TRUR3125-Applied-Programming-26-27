package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.network_layer.IPPacket;

/**
 * Defines handles for ICMP messages.
 */
public interface ICMPMessageHandler {

	boolean matches(ICMPMessage message);
	
	void handle(IPPacket packet,ICMPMessage message);
}
