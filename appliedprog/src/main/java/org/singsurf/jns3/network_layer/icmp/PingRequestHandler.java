package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class PingRequestHandler implements ICMPMessageHandler {
	String name;
	IPAddress hostAddress;
	PacketSender sender;
	
	public PingRequestHandler(AddressableDevice addr, PacketSender sender) {
		this.name = addr.getName();
		this.sender = sender;
		this.hostAddress = addr.getIPAddress();
	}

	@Override
	public boolean matches(ICMPMessage message) {
		if (message.getType() == 8) {
			return true;
		}
		return false;
	}

	@Override
	public void handle(IPPacket packet, ICMPMessage message) {
		var sourceAddress = packet.getSourceAddress();
		var destinationAddress = packet.getDestinationAddress();
		if(destinationAddress.equals(hostAddress)) {
			Jns3Logger.logRec(name, 
					"Received ping request from " + sourceAddress 
					+ " " + message.toString()
					+ " TTL: " + packet.getTimeToLive() + " ticks");

			// Create an ICMP Echo Reply message
			ICMPMessage replyMessage = ICMPMessage.createPingReply(message.getRestLow(), message.getRestHigh());
			IPPacket replyPacket = new IPPacket(
					IPPacket.IMCP, 64, 
					hostAddress, sourceAddress,
					replyMessage.getBytes());
			sender.sendPacket(replyPacket);
		} else {
			// Forward the packet to the next hop
			sender.sendPacket(packet);
		}
		
	}

}
