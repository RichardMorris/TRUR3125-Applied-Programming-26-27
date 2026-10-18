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

/**
 * Controller for handling ping requests and replies.
 * Can start a ping request and handle incoming ping replies.
 */
public class PingController implements  ICMPMessageHandler{
	static short currentIdentifier=0;
	static final short TRACEROUTE_OFFSET = (short) 0x4000; 
	short identifier = 0;
	short sequenceNumber = 0;
	PC pc;
	
	record SentPing(ICMPMessage request, long sentTime, IPAddress dest, int ttl) {}
	Map<Short, SentPing> pingRequests = new HashMap<>();

	record PingReplyRecord(IPAddress ip, ICMPMessage msg, int ttl) {}
	List<PingReplyRecord> completed = new ArrayList<>();

	private PingController(PC pc2, short identifier) {
		this.pc = pc2;
		this.identifier = identifier;
	}

	public void sendPingRequest(IPAddress destinationIP,int ttl) {
		++sequenceNumber;
		var packet = makePingPacket(identifier, sequenceNumber, destinationIP, ttl);
		pc.display("PING " + destinationIP + " " + packet.getTotalLength()+ " bytes of data.");
		pc.sendPacket(packet);
	}

	public void sendTraceroute(IPAddress destinationIP) {
		++sequenceNumber;
		var packet = makePingPacket(identifier+ TRACEROUTE_OFFSET, sequenceNumber, destinationIP, 1);
		pc.display(String.format("traceroute to %s, 64 hops max", destinationIP));
		pc.sendPacket(packet);
	}

	private IPPacket makePingPacket(int id, int code, IPAddress destinationIP, int ttl) {
		// Create a ping request message
		ICMPMessage pingRequest = ICMPMessage.createPingRequest((short) id,(short) code);
		// Create an IP packet with the ping request as payload
		IPPacket packet = new IPPacket(
				IPPacket.IMCP,ttl,
				pc.getIPAddress(), destinationIP, pingRequest.getBytes());
		// Send the packet using the provided PacketSender
		pingRequests.put((short) code,
				new SentPing(pingRequest, GlobalWallClock.getTime(), destinationIP,ttl)); // Store the sequence number for tracking
		return packet;
	}


	@Override
	public boolean matches(ICMPMessage message) {
		return (message.isPingReply() 
				|| message.isTTLExpired() );
	}

	@Override
	public void handle(IPPacket packet, ICMPMessage message) {
		var sourceAddress = packet.getSourceAddress();
		var destinationAddress = packet.getDestinationAddress();
//		var rest = message.getRestOfHeader();
		if(!destinationAddress.equals(pc.getIPAddress())) {
			Jns3Logger.logError(pc.getName(), "Received ping reply not for this host: " + packet);
			return;
		}
		var sequenceNumber = message.getRestHigh();
		if(!pingRequests.containsKey(sequenceNumber)) {
			Jns3Logger.logError(pc.getName(), 
					"Received orphaned ping reply from " + sourceAddress 
					+ " " + message.toString());
			return;
		}
		
		var sentReq = pingRequests.get(sequenceNumber);
		var sentTime = sentReq.sentTime();
		var currentTime = GlobalWallClock.getTime();
		var rtt = currentTime - sentTime;
		
		if(sentReq.request().getIdentifier() >= TRACEROUTE_OFFSET) {
			receiveTraceroute(packet, sentReq, rtt);	
			return;
		}
		if(message.isPingReply()) {				
				Jns3Logger.logRec(pc.getName(), 
						"Received ping reply from " + sourceAddress 
						+ " " + message.toString()
						+ " RTT: " + rtt + " ticks");
				// 64 bytes from 106.8.139.92: icmp_seq=3 ttl=47 time=294 ms
				pc.display(String.format(
						"%d bytes from %s: icmp_seq=%d ttl=%d time=%d ticks",
						packet.getTotalLength(), sourceAddress,
						message.getSequenceNumber(), packet.getTimeToLive(), rtt));

				pingRequests.remove(sequenceNumber); // Remove the entry after processing
				completed.add(new PingReplyRecord(sourceAddress, message, packet.getTimeToLive())); // Store the received ping reply
		} else {
			Jns3Logger.logError(pc.getName(), 
					"Received error from " + sourceAddress 
					+ " " + message.toString()
					+ " RTT: " + rtt + " ticks");
		
		}

		
	}

	private void receiveTraceroute(IPPacket packet, SentPing sentReq, long rtt) {
		//   1   172.22.64.1  1.439ms  0.612ms  0.358ms
		pc.display(String.format("%4d\t%s\t%d ticks",
			sentReq.ttl(), packet.getSourceAddress(), rtt));
		if(packet.getSourceAddress().equals(sentReq.dest())) {
			Jns3Logger.logInfo(pc.getName(),"Traceroute completed");
			return;
		}
		
		var pack = makePingPacket(identifier+ TRACEROUTE_OFFSET,
				sentReq.request().getCode(), sentReq.dest(), sentReq.ttl()+1);
		pc.sendPacket(pack);
	}
	
	static PingController newInstance(PC pc) {
		PingController controller = new PingController(pc, currentIdentifier++);
		return controller;
	}
}
