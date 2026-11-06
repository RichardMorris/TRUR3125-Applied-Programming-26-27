package org.singsurf.jns3.transport_layer.udp;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.PacketHandler;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Controller for incoming/outbound data
 * sent via a {@link DatagramSocket} using the UDP protocol.
 */
public class UDPController implements PacketHandler, NamedDevice {
	static boolean LOG_MESSAGES = false;
	String name;
	AddressableDevice device;
	PacketSender packetSender;
	Map<IPAddress,Map<Short, ProtocolSocket<UDPDatagram>>> bindings = new HashMap<>();
	UDPExtractor udpExtractor = UDPExtractor.getInstance();
	
	UDPController(AddressableDevice device) {
		this.name = device.getName() + "-UDPController";
		this.device = device;
	}
	
	void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	private void receive(IPPacket packet) {
		var datagram = udpExtractor.extract(packet);
		var lookupAddress = 
				packet.getDestinationAddress().equals(device.getMulticastAddress())
					|| packet.getDestinationAddress().equals(IPProtocol.LIMITED_MULTICAST_ADDRESS)
				? packet.getDestinationAddress() 
				: packet.getSourceAddress();
		
		
		var table = bindings.get(lookupAddress);
		if(table == null) {
			// is the packet destined for the limited broadcast address?
			table = bindings.get(IPProtocol.LIMITED_MULTICAST_ADDRESS);
			if(table == null) {
				if(packet.getDestinationAddress().equals(device.getMulticastAddress())) {
					Jns3Logger.logInfo(name, "No socket found for port " + datagram.getDestinationPort());				
				} else {
					Jns3Logger.logError(name, "No sockets found for IP address " + lookupAddress);
				}
				return;
			}
		}
		var socket = table.get(datagram.getDestinationPort());
		if(socket == null) {
			// Not an error if to multicast and we dont have a socket for that port
			if(packet.getDestinationAddress().equals(device.getMulticastAddress())) {
				Jns3Logger.logInfo(name, "No socket found for port " + datagram.getDestinationPort());				
			} else {
				Jns3Logger.logError(name, "No socket found for port " + datagram.getDestinationPort());
			}
			
			return;
		}
		if(LOG_MESSAGES)
			Jns3Logger.logInfo(name, "Received datagram from " + packet.getSourceAddress() + " on port " + datagram.getDestinationPort());
		socket.receive(datagram);
	}
	
	void sendDatagram(IPAddress boundAddress, short port, UDPDatagram datagram) {

		var table = bindings.get(boundAddress);
		if(table == null) {
			Jns3Logger.logError(name, "No sockets found for address " + boundAddress);
			return;
		}
		var binding = table.get(port);

		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + port);
			return;
		}
		var packet = new IPPacket(IPPacket.UDP, 64,
				device.getIPAddress(), boundAddress, datagram.getBytes()); 
		packetSender.send(packet);		  
	}

	void bind(IPAddress ip, short port, ProtocolSocket<UDPDatagram> socket) {
		var table = bindings.computeIfAbsent(ip, k -> new HashMap<>());
		table.put(port, socket);
	}

	void unbind(ProtocolSocket<UDPDatagram> datagramSocket) {
		var table = bindings.get(datagramSocket.getAddress());
		if(table != null) {
			table.remove(datagramSocket.getPort());
		}		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public HandlerStatus handle(IPPacket packet) {
		if(!udpExtractor.isExtractable(packet)) {
			return HandlerStatus.CONTINUE;
		}
		receive(packet);
		return HandlerStatus.COMPLETED;
	}
	
}
