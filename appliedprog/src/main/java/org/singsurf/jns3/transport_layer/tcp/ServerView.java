package org.singsurf.jns3.transport_layer.tcp;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;

/**
 * The client-side view of the TCP protocol. 
 *
 */
public class ServerView {
	enum State {
		Closed, SynSent, Established, FinWait1, FinWait2, TimeWait
	}

	TCPProtocol tcpProtocol;

	InternetMessage recombinedMessage;
	
	int initialSeqNum;
	int currentSeqNum;
	
	int initialRemoteSeqNum;
	int currentRemoteSeqNum;
	
	IPAddress remoteIP;
	short remotePort;
	
	IPAddress localIP;
	short localPort;

	Random random = new Random();

	Queue<TCPSegment> outOfOrderSegments = new ArrayDeque<>();

	public ServerView(TCPProtocol tcpProtocol, TCPSegment datagram) {
		this.tcpProtocol = tcpProtocol;
		this.initialSeqNum = random.nextInt();
		this.currentSeqNum = initialSeqNum;
		this.initialRemoteSeqNum = datagram.getSequenceNumber();
		this.currentRemoteSeqNum = initialRemoteSeqNum+1;
		
		this.remoteIP = datagram.getPsudoHeader().sourceIP();
		this.remotePort = datagram.getSourcePort();
		this.localIP = datagram.getPsudoHeader().destinationIP();
		this.localPort = datagram.getDestinationPort();
	}

	
	public void receiveAck(TCPSegment datagram) {
		currentRemoteSeqNum = datagram.getSequenceNumber();
		currentSeqNum = datagram.getAcknowledgementNumber();
	}

	void sendSynAck() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				initialSeqNum, currentRemoteSeqNum, 
				(byte) (TCPSegment.FLAG_SYN | TCPSegment.FLAG_ACK),
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
	}

	public void receivePsh(TCPSegment datagram) {
		var seqNo = datagram.getSequenceNumber();
		var ackNo = datagram.getAcknowledgementNumber();
		
		if(ackNo != currentSeqNum) {
			// out of order ack
			Jns3Logger.logError(tcpProtocol.getName(), "Out of order ACK received from " + remoteIP + ":" + remotePort);
			return;
		}
		if(seqNo == currentRemoteSeqNum) {
			var contents = datagram.getContents();
			currentRemoteSeqNum += contents.length;			
			sendAck();
			
			tcpProtocol.receive(new InternetMessage("http",
					remoteIP, localIP, remotePort, localPort, contents));
		} else {
			// out of order segment
			outOfOrderSegments.add(datagram);
		}
	}

	void sendAck() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				initialSeqNum, currentRemoteSeqNum, 
				(byte) (TCPSegment.FLAG_SYN | TCPSegment.FLAG_ACK),
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
	}

	public int getInitialSeqNum() {
		return initialSeqNum;
	}

	public int getCurrentSeqNum() {
		return currentSeqNum;
	}

	public int getInitialRemoteSeqNum() {
		return initialRemoteSeqNum;
	}

	public int getCurrentRemoteSeqNum() {
		return currentRemoteSeqNum;
	}

	public IPAddress getRemoteIP() {
		return remoteIP;
	}

	public short getRemotePort() {
		return remotePort;
	}

	public IPAddress getLocalIP() {
		return localIP;
	}

	public short getLocalPort() {
		return localPort;
	}




	
}
