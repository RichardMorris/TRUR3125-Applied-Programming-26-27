package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.network_layer.IPPacket;

public interface ICMPMessageHandler {

	boolean matches(ICMPMessage message);
	
	void handle(IPPacket packet,ICMPMessage message);
}
