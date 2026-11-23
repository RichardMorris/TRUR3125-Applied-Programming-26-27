package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;

/**
 * The client-side view of the TCP protocol. 
 *
 */
public class ClientView implements TickListener {
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

	State state = State.Closed;
	
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
		
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT, this);
	}

	void sendSyn() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN, 
				tcpProtocol.windowSize,
				new byte[0],
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		state = State.SynSent;
		tcpProtocol.sender.send(packet);
	}

	public void receiveSynAck(TCPSegment datagram) {
		int synNo = datagram.getSequenceNumber();
		int ackNo = datagram.getAcknowledgementNumber();
		initialRemoteSeqNum = synNo;
		currentRemoteSeqNum = initialRemoteSeqNum+1;
		currentSeqNum = ackNo;
		sendAck();
		state = State.Established;
	}

	public void sendAck() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				currentSeqNum, currentRemoteSeqNum, 
				TCPSegment.FLAG_ACK, 
				tcpProtocol.windowSize,
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
	}

	@Override
	public void onTick() {
		if(state == State.Established) {
			if(message != null) {
				Jns3Logger.logSend(tcpProtocol.getName(), "Sending message to " + remoteIP + ":" + remotePort);
				sendPsh(message.getContents());
				message = null;
			}
		}
	}

	void sendPsh(byte[] contents) {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				currentSeqNum, currentRemoteSeqNum, 
				(byte) (TCPSegment.FLAG_ACK | TCPSegment.FLAG_PSH), 
				tcpProtocol.windowSize,
				contents, 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
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
