package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPPacket;

/**
 *  Default handler that logs the message.
 */
public class ICMPMessageLogger implements ICMPMessageHandler {
	String name;

	public ICMPMessageLogger(String name) {
		this.name = name;
	}
	
	@Override
	public boolean matches(ICMPMessage message) {
		return true;
	}

	@Override
	public void handle(IPPacket packet, ICMPMessage message) {
		Jns3Logger.logRec(name, "Received ICMP message: " + message + " " + packet);
		
	}

}
