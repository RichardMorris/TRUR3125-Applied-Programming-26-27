package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;

/**
 *  Default handler that logs the message.
 */
public class ICMPMessageLogger implements ICMPMessageHandler {
	String name;

	public ICMPMessageLogger(String name) {
		this.name = name;
	}
	
	@Override
	public HandlerStatus handle(ICMPMessage message) {
		Jns3Logger.logRec(name, "Received ICMP message: " + message + " " + 
				message.getPsudoHeader().sourceIP() + " -> " + message.getPsudoHeader().destIP());
		return HandlerStatus.COMPLETED;
	}

}
