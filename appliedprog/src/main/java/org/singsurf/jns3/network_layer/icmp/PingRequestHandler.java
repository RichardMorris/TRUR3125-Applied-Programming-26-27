package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class PingRequestHandler implements ICMPMessageHandler {
	String name;
	PacketSender sender;
	IPAddress hostAddress;

	public PingRequestHandler(String name, IPAddress hostAddress, PacketSender sender) {
		this.name = name;
		this.sender = sender;
		this.hostAddress = hostAddress;
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
		var rest = message.getRestOfHeader();
		if(destinationAddress.equals(hostAddress)) {
			Jns3Logger.logRec(name, 
					"Received ping request from " + sourceAddress 
					+ " " + message.toString()
					+ " TTL: " + packet.getTimeToLive() + " ms");

			// Create an ICMP Echo Reply message
			ICMPMessage replyMessage = new PingReply(rest);
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
