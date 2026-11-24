package org.singsurf.jns3.transport_layer.tcp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;

/**
 * A hosts view of a TCP channel between ports on two devices.
 */
public class TCPChannel implements TickListener {
	enum State {
		Closed, SynSent, Established, FinWait1, FinWait2, TimeWait
	}
	
	TCPProtocol tcpProtocol;
	
	/**
	 * The base our sequence numbers.
	 */
	int initialSeqNum;
	
	/**
	 * The current sequence number acknowledged by server.
	 */
	int currentSeqNum;
	
	/**
	 * Next sequence number used for queued messages.
	 */
	int nextSeqNum=1;
	
	int initialRemoteSeqNum;
	int currentRemoteSeqNum;
	
	IPAddress remoteIP;
	short remotePort;
	
	IPAddress localIP;
	short localPort;

	State state = State.Closed;
	
	record WaitingMessage(int synNum, byte[] content) {}
	
	Deque<WaitingMessage> messageQueue = new ArrayDeque<>();
	
	Deque<TCPSegment> outOfOrderSegments = new ArrayDeque<>();

	
	Random random = new Random();

	WaitingMessage currentMessage;
	
	/**
	 * Constructor for a client-side view of the TCP protocol. 
	 * This is used when a client first tries to connect to an
	 * unrecognised server.
	 * Source and destination parameters are found from the message
	 * and the message is queued for delivery once the handshake is 
	 * completed. 
	 * @param tcpProtocol
	 * @param message initial message, including parameters for transfer.
	 */

	public TCPChannel(TCPProtocol tcpProtocol, SocketMessage message) {
		this.tcpProtocol = tcpProtocol;
		this.initialSeqNum = random.nextInt();
		this.currentSeqNum = initialSeqNum;
		this.nextSeqNum = initialSeqNum +1;
		this.initialRemoteSeqNum = 0;
		this.currentRemoteSeqNum = 0;
		
		this.remoteIP = message.getDestIP();
		this.remotePort = message.getDestPort();
		this.localIP = message.getSrcIP();
		this.localPort = message.getSrcPort();
		queue(message);
		
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT+1, this);
	}
	
	/**
	 * Constructor for a server-side view of the TCP protocol. 
	 * This is used when a server receives a SYN from a client
	 * and needs to respond with a SYN-ACK. 
	 * Source and destination parameters are found from the datagram.
	 * @param tcpProtocol
	 * @param datagram initial SYN datagram received from client.
	 */
	public TCPChannel(TCPProtocol tcpProtocol, TCPSegment datagram) {
		this.tcpProtocol = tcpProtocol;
		this.initialSeqNum = random.nextInt();
		this.currentSeqNum = initialSeqNum;
		this.nextSeqNum = initialSeqNum +1;
		this.initialRemoteSeqNum = datagram.getSequenceNumber();
		this.currentRemoteSeqNum = initialRemoteSeqNum+1;
		
		this.remoteIP = datagram.getPsudoHeader().sourceIP();
		this.remotePort = datagram.getSourcePort();
		this.localIP = datagram.getPsudoHeader().destinationIP();
		this.localPort = datagram.getDestinationPort();
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT+1, this);
	}

	// Handshake methods

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

	void sendSynAck() {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				localIP, remoteIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				localPort, remotePort,
				initialSeqNum, currentRemoteSeqNum, 
				(byte) (TCPSegment.FLAG_SYN | TCPSegment.FLAG_ACK),
				tcpProtocol.windowSize,
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		tcpProtocol.sender.send(packet);
		state = State.Established;
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

	
	
	void queue(SocketMessage msg) {
		messageQueue.add(new WaitingMessage(nextSeqNum,msg.contents));
		nextSeqNum += msg.getContents().length;
	}

	@Override
	public void onTick() {
		if(state == State.Established) {
			var item = messageQueue.poll();			
			if(item != null) {
				var content = item.content();
				var length = content.length;
				var msgRelNo = item.synNum() - initialSeqNum;
				var curRelNo = currentSeqNum - initialSeqNum;
				Jns3Logger.logInfo(tcpProtocol.getName(), 
						"sending segment "+msgRelNo+" cur "+curRelNo);
				if(item.synNum() == currentSeqNum) {
					if(length > tcpProtocol.windowSize) {
						byte[] head = new byte[tcpProtocol.windowSize];
						byte[] tail = new byte[length-tcpProtocol.windowSize];
						System.arraycopy(content, 0, head, 0, tcpProtocol.windowSize);
						System.arraycopy(content, tcpProtocol.windowSize, tail, 0, length-tcpProtocol.windowSize);
						content = head;
						var tailMsg = new WaitingMessage(item.synNum()+tcpProtocol.windowSize, tail);
						messageQueue.offerFirst(tailMsg);
					}
					Jns3Logger.logSend(tcpProtocol.getName(), "Sending message to " + remoteIP + ":" + remotePort);
					sendPsh(content);
				} else if(before(item.synNum(),currentSeqNum,initialSeqNum)) {
					Jns3Logger.logError(tcpProtocol.getName(), 
							"SeqNumber is 'before' current");
				} else {
					Jns3Logger.logInfo(tcpProtocol.getName(), "Previous message not completed");
					var good = messageQueue.offerFirst(item); 
					if(!good) {
						Jns3Logger.logError(tcpProtocol.getName(), "Failed to requeue message");
					}
				}
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
	
	public void receiveAck(TCPSegment datagram) {
		currentRemoteSeqNum = datagram.getSequenceNumber();
		currentSeqNum = datagram.getAcknowledgementNumber();
		if(currentMessage != null) {
			var seqNo = currentMessage.synNum();
			var length = currentMessage.content().length;
			var endNo = seqNo + length;
			if(endNo == currentSeqNum) {
				Jns3Logger.logInfo(tcpProtocol.getName(), "Message completed " + remoteIP + ":" + remotePort);
				currentMessage = null;
			} else if( before(currentSeqNum, endNo, initialSeqNum) ) {
				Jns3Logger.logInfo(tcpProtocol.getName(), "Message partially completed " + remoteIP + ":" + remotePort);
			} else {
				Jns3Logger.logError(tcpProtocol.getName(), "Unexpected ACK received from " + remoteIP + ":" + remotePort);
			}
		}
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
			
			tcpProtocol.receive(new SocketMessage("http",
					remoteIP, remotePort, localIP, localPort, contents));
		} else {
			// out of order segment
			outOfOrderSegments.add(datagram);
		}
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


	@Override
	public String toString() {
		return "TCPChannel [remoteIP=" + remoteIP + ", remotePort=" + remotePort + ", localIP=" + localIP
				+ ", localPort=" + localPort + ", state=" + state + "]";
	}
	
	/**
	 * Test if {@code num < current} taking account the cyclic nature 
	 * of sequence numbers.
	 * @param num
	 * @param current
	 * @param initial
	 * @return
	 */
	static boolean before(int num, int current, int initial) {
		return (num - initial) < (current-initial);
	}



}
