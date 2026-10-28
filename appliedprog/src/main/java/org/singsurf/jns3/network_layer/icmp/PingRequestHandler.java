package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Replies to ping/traceroute requests. Displays a message
 * on the terminal.
 */
public class PingRequestHandler implements ICMPMessageHandler {
	String name;
	AddressableDevice device;
	PacketSender sender;
	
	public PingRequestHandler(AddressableDevice addr, PacketSender sender) {
		this.name = addr.getName();
		this.device = addr;
		this.sender = sender;
	}


	private void handle2(ICMPMessage message) {
		var sourceAddress = message.getPsudoHeader().sourceIP();
		var destinationAddress = message.getPsudoHeader().destIP();
		if(destinationAddress.equals(device.getIPAddress())) {
			Jns3Logger.logRec(name, 
					"Received ping request from " + sourceAddress 
					+ " " + message.toString()
					+ " TTL: " + message.getPsudoHeader().ttl() + " ticks");

			// Create an ICMP Echo Reply message
			ICMPMessage replyMessage = ICMPMessage.createPingReply(message.getRestLow(), message.getRestHigh());
			IPPacket replyPacket = new IPPacket(
					IPPacket.IMCP, 64, 
					device.getIPAddress(), sourceAddress,
					replyMessage.getBytes());
			sender.send(replyPacket);
		} else {
			sender.send(message.getPsudoHeader().packet());
		}
		
	}

	@Override
	public HandlerStatus handle(ICMPMessage message) {
		if (message.getType() != 8) {
			return HandlerStatus.CONTINUE;
		}
		handle2(message);
			// Forward the packet to the next hop

		return HandlerStatus.COMPLETED;
	}

}
