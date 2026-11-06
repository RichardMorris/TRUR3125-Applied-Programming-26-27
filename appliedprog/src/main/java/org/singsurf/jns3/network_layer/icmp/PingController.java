package org.singsurf.jns3.network_layer.icmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.Terminal;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Controller for handling ping requests and replies.
 * Can start a ping/traceroute requests and handle incoming ping replies
 * and TTL expired messages.
 * <p>
 * Ping requests have: type: 8, code: 0 and a rest section containing
 * a two byte "identifier" unique to each device,
 *  and a two byte "sequence number" unique to each ping request.
 * our implementation uses identifiers in the range 0-0x3FFF,
 * for ping requests and 0x4000-0x7FFF for traceroute requests.
 * <p>
 * See <a href="../../../../../../../Week11.md">Week 11</a>.
 */
public class PingController implements  ICMPMessageHandler {
	static short currentIdentifier=0;
	static final short TRACEROUTE_OFFSET = (short) 0x4000; 
	short identifier = 0;
	short sequenceNumber = 0;
	AddressableDevice pc;
	PacketSender packetSender;
	Terminal console;
	Map<Short, SentPing> sentPings = new HashMap<>();
	List<PingReplyRecord> completed = new ArrayList<>();
	
	record SentPing(ICMPMessage request, int sentTime, IPAddress dest, int ttl) {}

	record PingReplyRecord(IPAddress ip, ICMPMessage msg, int rtt) {}

	private PingController(AddressableDevice pc2, Terminal term, short identifier) {
		this.pc = pc2;
		this.identifier = identifier;
		this.console = term;
	}

	public void sendPingRequest(IPAddress destinationIP,int ttl) {
		++sequenceNumber;
		var packet = makePingRequestPacket(identifier, sequenceNumber, destinationIP, ttl);
		console.display("PING " + destinationIP + " " + packet.getTotalLength()+ " bytes of data.");
		if(!ICMPPacketExtractor.getInstance().isExtractable(packet)) {
			Jns3Logger.logError(pc.getName(), "Bad packet: " + packet);
		}
		packetSender.send(packet);
	}

	public void sendTraceroute(IPAddress destinationIP) {
		++sequenceNumber;
		var packet = makePingRequestPacket(identifier+ TRACEROUTE_OFFSET, sequenceNumber, destinationIP, 1);
		console.display(String.format("traceroute to %s, 64 hops max", destinationIP));
		packetSender.send(packet);
	}

	private IPPacket makePingRequestPacket(int id, int code, IPAddress destinationIP, int ttl) {
		// Create a ping request message
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) id, (short) code);
		// Create an IP packet with the ping request as payload
		IPPacket packet = new IPPacket(
				IPPacket.IMCP,ttl,
				pc.getIPAddress(), destinationIP, pingRequest.getBytes());
		// Send the packet using the provided PacketSender
		sentPings.put((short) code,
				new SentPing(pingRequest, GlobalWallClock.getTime(), destinationIP,ttl)); // Store the sequence number for tracking
		return packet;
	}

	@Override
	public HandlerStatus handle(ICMPMessage msg) {
		if(msg.isPingReply()) {
			handle2(msg, msg.getIdentifier(), msg.getSequenceNumber());
			return HandlerStatus.COMPLETED;
		} else if(msg.isTTLExpired()) {
			// only handle TTL expired messages for our sent pings
			var pkt = IPPacket.fromBytes(msg.getContents());
			if(pkt.getProtocol() != IPPacket.IMCP) {
				Jns3Logger.logInfo(pc.getName(), "Received TTL expired message not for ICMP: " + msg);
				return HandlerStatus.CONTINUE;
			}
			var originalMsg = ICMPPacketExtractor.getInstance().extract(pkt);
			if(originalMsg.getType() != ICMPMessage.PING_REQUEST_TYPE) {
				Jns3Logger.logInfo(pc.getName(), "Received TTL expired message not for ping request: " + msg);
				return HandlerStatus.CONTINUE;
			}
			if(originalMsg.getIdentifier() != identifier 
					&& originalMsg.getIdentifier() != identifier + TRACEROUTE_OFFSET) {
				Jns3Logger.logInfo(pc.getName(), "Received TTL expired message not for this host: " + msg);
				return HandlerStatus.CONTINUE;
			}
			handle2(msg,originalMsg.getIdentifier(), originalMsg.getSequenceNumber());
			return HandlerStatus.COMPLETED;
		}
		else {
			return HandlerStatus.CONTINUE;
		}
	}

	private void handle2(ICMPMessage message, short id, short seqNum) {
		var sourceAddress = message.psudoHeader.sourceIP();
		var destinationAddress = message.psudoHeader.destIP();
//		var rest = message.getRestOfHeader();
		if(!destinationAddress.equals(pc.getIPAddress())) {
			Jns3Logger.logError(pc.getName(), "Received ping reply not for this host: " + message);
			return;
		}
//		var sequenceNumber = message.getRestHigh();
		if(!sentPings.containsKey(seqNum)) {
			Jns3Logger.logError(pc.getName(), 
					"Received orphaned ping reply from " + sourceAddress 
					+ " " + message.toString());
			return;
		}
		
		var sentReq = sentPings.remove(seqNum);
		var sentTime = sentReq.sentTime();
		var currentTime = GlobalWallClock.getTime();
		var rtt = currentTime - sentTime;
		if(id >= TRACEROUTE_OFFSET) {
			receiveTraceroute(message, sentReq, rtt);	
			return;
		}
		if(message.isPingReply()) {				
				Jns3Logger.logRec(pc.getName(), 
						"Received ping reply from " + sourceAddress 
						+ " " + message.toString()
						+ " RTT: " + rtt + " ticks");
				// 64 bytes from 106.8.139.92: icmp_seq=3 ttl=47 time=294 ms
				console.display(String.format(
						"%d bytes from %s: icmp_seq=%d ttl=%d time=%d ticks",
						message.psudoHeader.length(), sourceAddress,
						message.getSequenceNumber(), message.psudoHeader.ttl(), rtt));

				completed.add(new PingReplyRecord(sourceAddress, message, rtt)); // Store the received ping reply
		} else {
			Jns3Logger.logError(pc.getName(), 
					"Received error from " + sourceAddress 
					+ " " + message.toString()
					+ " RTT: " + rtt + " ticks");
		}
	}

	private void receiveTraceroute(ICMPMessage message, SentPing sentReq, long rtt) {
		//   1   172.22.64.1  1.439ms  0.612ms  0.358ms
		console.display(String.format("%4d\t%s\t%d ticks",
			sentReq.ttl(), message.psudoHeader.sourceIP(), rtt));
		if(message.psudoHeader.sourceIP().equals(sentReq.dest())) {
			Jns3Logger.logInfo(pc.getName(),"Traceroute completed");
			return;
		}
		++sequenceNumber;
		var pack = makePingRequestPacket(identifier+ TRACEROUTE_OFFSET,
				sequenceNumber, sentReq.dest(), sentReq.ttl()+1);
		packetSender.send(pack);
	}
	

	public void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public static PingController newInstance(AddressableDevice pc, Terminal term) {
		PingController controller = new PingController(pc, term, currentIdentifier++);
		return controller;
	}

}
