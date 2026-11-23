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
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.generic_protocol.ProtocolVisitor;
import org.singsurf.jns3.generic_protocol.UpperSocketPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class TCPProtocol implements Protocol, LowerAdapter<IPPacket>, UpperSocketPort<InternetMessage> {
	String name;
	ProtocolDevice device;
	short windowSize = 2048;

	TCPSocketFactory socketFactory;
	List<LowerSocketAdapter<InternetMessage>> childProtocols = new ArrayList<>();
	PacketSender sender;
	Map<IPAddress,Map<Short, ProtocolSocket<InternetMessage>>> bindings = new HashMap<>();
	ProtocolDelegator<TCPSegment> delegator;
	TCPPacketHandler packetHandler;
	
	public record PortAddr(IPAddress ip, short port) {}
	
	Map<PortAddr, BothView> clientViews = new HashMap<>();
	
	Queue<InternetMessage> messageQueue = new LinkedList<>();
	
	Random random = new Random();
		
	public TCPProtocol(ProtocolDevice device) {
		this.device = device;
		this.name = device.getName() + "-TCP";
		socketFactory = new TCPSocketFactory(this);
		delegator = new ProtocolDelegator<TCPSegment>(name);
		delegator.addHandler(new SynHandler(this));
		delegator.addHandler(new SynAckHandler(this));
		delegator.addHandler(new ServerAckHandler(this));
		delegator.addHandler(new PushHandler(this));
		packetHandler = new TCPPacketHandler(this);
	}

	public void transmitMessage(InternetMessage message) {
		// Start the TCP connection handshake
		var destIP = message.getDestIP();
		var destPort = message.getDestPort();
		var portAddr = new PortAddr(destIP, destPort);
		
		var ent = clientViews.get(portAddr);
		if(ent != null) {
			messageQueue.add(message);
			return;
		}
		var clientView = new BothView(this,message);
		clientViews.put(
				new PortAddr(destIP, destPort), 
				clientView);	
		clientView.sendSyn();
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
		} else {
			Jns3Logger.logError("TCPProtocol", "Unbind failed for " + tcpSocket);
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

	public String getName() {
		return name;
	}

	public void receive(InternetMessage msg) {
		var table = bindings.get(device.getMulticastAddress());
		if(table != null) {
			var binding = table.get(msg.getDestPort());
			if(binding != null) {
				binding.receive(msg);
			} else {
				Jns3Logger.logError("TCPProtocol", "No binding for " + msg);
			}
		} else {
			Jns3Logger.logError("TCPProtocol", "No binding for " + msg);
		}
	}

	@Override
	public <R> R accept(ProtocolVisitor<R> visitor, IPPacket packet) {
		if(packet.getProtocol() != IPPacket.TCP) {
			return null;
		}
		var tcpSegment = TCPSegment.fromPacket(packet);
		return visitor.visit(this, tcpSegment);
	}

	public void setWindowSize(short windowSize) {
		this.windowSize = windowSize;
	}
}
