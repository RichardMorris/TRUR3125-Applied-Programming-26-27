package org.singsurf.jns3.network_layer.icmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Controller for handling ping requests and replies.
 * Can start a ping request and handle incoming ping replies.
 */
public class PingController implements PacketSender, ICMPMessageHandler{
	static short currentIdentifier=0;
	short indetifier = 0;
	short sequenceNumber = 0;
	PC pc;
	
	Map<Short, Integer> pingRequests = new HashMap<>();
	record PingReplyRecord(IPAddress ip, ICMPMessage msg, int ttl) {}
	List<PingReplyRecord> completed = new ArrayList<>();

	private PingController(PC pc2, short identifier) {
		this.pc = pc2;
		this.indetifier = identifier;
	}

	public void sendPingRequest(IPAddress destinationIP,int ttl) {
		++sequenceNumber;
		// Create a ping request message
		PingRequest pingRequest = new PingRequest(indetifier,sequenceNumber);
		// Create an IP packet with the ping request as payload
		IPPacket packet = new IPPacket(
				IPPacket.IMCP,ttl,
				pc.getIPAddress(), destinationIP, pingRequest.getBytes());
		// Send the packet using the provided PacketSender
		pingRequests.put(sequenceNumber, GlobalWallClock.getTime()); // Store the sequence number for tracking
		// PING 106.8.139.92 56 bytes of data.
		pc.display("PING " + destinationIP + " " + packet.getTotalLength()+ " bytes of data.");
		sendPacket(packet);
	}

	@Override
	public void sendPacket(IPPacket packet) {
		pc.sendPacket(packet);
	}


	@Override
	public boolean matches(ICMPMessage message) {
		return message.getType() == 0; // Type 0 is for ping replies
	}

	@Override
	public void handle(IPPacket packet, ICMPMessage message) {
		var sourceAddress = packet.getSourceAddress();
		var destinationAddress = packet.getDestinationAddress();
//		var rest = message.getRestOfHeader();
		if(destinationAddress.equals(pc.getIPAddress())) {
			var sequenceNumber = message.getRestHigh();
			if(pingRequests.containsKey(sequenceNumber)) {
				var sentTime = pingRequests.get(sequenceNumber);
				var currentTime = GlobalWallClock.getTime();
				var rtt = currentTime - sentTime;
				
				Jns3Logger.logRec(pc.getName(), 
						"Received ping reply from " + sourceAddress 
						+ " " + message.toString()
						+ " RTT: " + rtt + " ticks");
				pc.display("Received ping reply from " + sourceAddress + " with sequence number " + sequenceNumber + ". RTT: " + rtt + " ms");

				pingRequests.remove(sequenceNumber); // Remove the entry after processing
				completed.add(new PingReplyRecord(sourceAddress, message, packet.getTimeToLive())); // Store the received ping reply
			} else {
				Jns3Logger.logError(pc.getName(), 
						"Received ping reply from " + sourceAddress 
						+ " " + message.toString());
			}

		} else {
			// Log an error if the ping reply is not for this host
			Jns3Logger.logError(pc.getName(), "Received ping reply not for this host: " + packet);
		}
		
	}
	
	static PingController newInstance(PC pc) {
		PingController controller = new PingController(pc, currentIdentifier++);
		return controller;
	}
}
