package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class ICMPErrorHandler implements ICMPMessageHandler {
	String name;
	PacketSender sender;
	IPAddress hostAddress;
	

	public ICMPErrorHandler(String name, PacketSender sender, IPAddress hostAddress) {
		this.name = name;
		this.sender = sender;
		this.hostAddress = hostAddress;
	}
	
	@Override
	public boolean matches(ICMPMessage message) {
		var type = message.getType();
		if (type == 3 || type == 4 || type == 5 || type == 11 || type == 12) {
			return true;
		}
		return false;
	}

	@Override
	public void handle(IPPacket packet, ICMPMessage message) {
		if(packet.getDestinationAddress().equals(hostAddress)) {
			Jns3Logger.logError(name, "ICMP error packet is for this host: " + packet);
		} else {
			Jns3Logger.logError(name, "ICMP error packet is not for this host: " + packet);
			sender.sendPacket(packet);
		}
	}

}
