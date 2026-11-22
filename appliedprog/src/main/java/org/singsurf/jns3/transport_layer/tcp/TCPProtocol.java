package org.singsurf.jns3.transport_layer.tcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.generic_protocol.UpperSocketPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class TCPProtocol implements LowerAdapter<IPPacket>, UpperSocketPort<InternetMessage> {
	String name;
	ProtocolDevice device;

	TCPSocketFactory socketFactory;
	List<LowerSocketAdapter<InternetMessage>> childProtocols = new ArrayList<>();
	PacketSender sender;
	Map<IPAddress,Map<Short, ProtocolSocket<InternetMessage>>> bindings = new HashMap<>();
	ProtocolDelegator<TCPSegment> delegator;
	TCPPacketHandler packetHandler;
	
	public record PortAddr(IPAddress ip, short port) {}
	
	record MsgSynAck(InternetMessage message, int seqNum, int ackNum) {}
	Map<PortAddr, MsgSynAck> waitingMessages = new HashMap<>();
	Map<PortAddr,Integer> initialSynNums = new HashMap<>();
	
	record SegmentSynAck(TCPSegment segment, int seqNum, int ackNum) {}
	Map<PortAddr, List<SegmentSynAck>> receivedSegments = new HashMap<>();
	Map<PortAddr, Integer> intialAckNums = new HashMap<>();
	
	Queue<InternetMessage> messageQueue = new LinkedList<>();
	
	Random random = new Random();
		
	public TCPProtocol(ProtocolDevice device) {
		this.device = device;
		this.name = device.getName() + "-TCP";
		socketFactory = new TCPSocketFactory(this);
		delegator = new ProtocolDelegator<TCPSegment>(name);
		delegator.addHandler(new SynHandler(this));
		packetHandler = new TCPPacketHandler(this);
	}

	public void transmitMessage(InternetMessage message) {
		// Start the TCP connection handshake
		int seqNum = random.nextInt();
		int ackNum = 0;
		
		var srcIP = message.getSrcIP();
		var destIP = message.getDestIP();
		var srcPort = message.getSrcPort();
		var destPort = message.getDestPort();
		var portAddr = new PortAddr(destIP, destPort);
		
		var ent = waitingMessages.get(portAddr);
		if(ent != null) {
			messageQueue.add(message);
			return;
		}
		waitingMessages.put(
				new PortAddr(destIP, destPort), 
				new MsgSynAck(message, seqNum, ackNum));
		
		sendSyn(seqNum, ackNum, srcIP, srcPort, destIP, destPort);
	}

	void sendSyn(int seqNum, int ackNum, IPAddress srcIP, short srcPort, IPAddress destIP, short destPort) {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				srcIP, destIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				seqNum, ackNum, 
				TCPSegment.FLAG_SYN, new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		sender.send(packet);
	}

	void receiveSyn(TCPSegment datagram) {
		var portAddr = new PortAddr(
				datagram.getPsudoHeader().sourceIP(), 
				datagram.getSourcePort());
		int synNo = datagram.getSequenceNumber();
		int ackNo = random.nextInt();
		
		var ent = receivedSegments.get(portAddr);
		if(ent != null) {
			Jns3Logger.logError("TCPProtocol", "Dupe SYN received for " + portAddr);
			return;
		}
		initialSynNums.put(portAddr, synNo);
		intialAckNums.put(portAddr, ackNo);
		receivedSegments.put(portAddr, new ArrayList<>());
				
		sendSynAck(
				datagram.getPsudoHeader().destinationIP(), datagram.getDestinationPort(),
				datagram.getPsudoHeader().sourceIP(), datagram.getSourcePort(),
				synNo, ackNo);		
	}

	 
	void sendSynAck(IPAddress srcIP, short srcPort, 
			IPAddress destIP, short destPort,
			int synNo, int ackNo) {
		var pseudoHeader = new TCPSegment.PseudoHeader(
				srcIP, destIP, IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				synNo, ackNo, 
				(byte) (TCPSegment.FLAG_SYN | TCPSegment.FLAG_ACK),
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		sender.send(packet);
	}
	
	
	public void receiveSynAck(TCPSegment datagram) {
		// TODO Auto-generated method stub
		
	}


	// Socket binding methods
	
	void bind(IPAddress ip, short port, TCPSocket tcpSocket) {
		var table = bindings.computeIfAbsent(ip, k -> new HashMap<>());
		table.put(port, tcpSocket);
	}

	void unbind(TCPSocket tcpSocket) {
		var table = bindings.get(tcpSocket.getAddress());
		if(table != null) {
			table.remove(tcpSocket.getPort());
		}		
	}

	
	// Port and Adapter methods

	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(packetHandler);
	}

	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		this.sender= (PacketSender) sender; 
	}
	
	@Override
	public void register(LowerSocketAdapter<InternetMessage> adapter) {
		childProtocols.add(adapter);
		adapter.setSocketFactory(socketFactory);
	}

	@Override
	public List<LowerSocketAdapter<InternetMessage>> getChildProtocols() {
		return childProtocols;
	}





}
