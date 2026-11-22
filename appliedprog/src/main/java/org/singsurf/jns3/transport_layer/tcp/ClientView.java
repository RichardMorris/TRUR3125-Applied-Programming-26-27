package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;

/**
 * The client-side view of the TCP protocol. 
 *
 */
public class ClientView {
	enum State {
		Closed, SynSent, Established, FinWait1, FinWait2, TimeWait
	}
	
	TCPProtocol tcpProtocol;
	
	InternetMessage message;
	
	int initialSeqNum;
	int currentSeqNum;
	
	int initialRemoteSeqNum;
	int currentRemoteSeqNum;
	
	IPAddress remoteIP;
	short remotePort;
	
	IPAddress localIP;
	short localPort;


	Random random = new Random();

	public ClientView(TCPProtocol tcpProtocol, InternetMessage message) {
		this.tcpProtocol = tcpProtocol;
		this.message = message;
		this.initialSeqNum = random.nextInt();
		this.currentSeqNum = initialSeqNum;
		this.initialRemoteSeqNum = 0;
		this.currentRemoteSeqNum = 0;
		
		this.remoteIP = message.getDestIP();
		this.remotePort = message.getDestPort();
		this.localIP = message.getSrcIP();
		this.localPort = message.getSrcPort();
	}

	void sendSyn() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN, new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
	}

	public void receiveSynAck(TCPSegment datagram) {
		int synNo = datagram.getSequenceNumber();
		int ackNo = datagram.getAcknowledgementNumber();
		initialRemoteSeqNum = synNo;
		currentRemoteSeqNum = initialRemoteSeqNum+1;
		currentSeqNum = ackNo;
	}

	public InternetMessage getMessage() {
		return message;
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
