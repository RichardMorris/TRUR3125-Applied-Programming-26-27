package org.singsurf.jns3.transport_layer.udp;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketHandler;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Controller for incoming/outbound data
 * sent via a {@link DatagramSocket} using the UDP protocol.
 */
public class UDPController implements PacketHandler, NamedDevice {
	String name;
	AddressableDevice device;
	PacketSender packetSender;
	Map<IPAddress,Map<Short, SocketBinding>> bindings = new HashMap<>();
	UDPExtractor udpExtractor = UDPExtractor.getInstance();
	
	/**
	 * Represents a binding between a socket and this controller,
	 */
	record SocketBinding(IPAddress ip, short port, DatagramSocket socket) {}
	
	/**
	 * 
	 */
	public record DatagramPacket(IPAddress source, IPAddress dest, UDPDatagram datagram) {}

	public UDPController(AddressableDevice device, PacketSender packetSender) {
		this.name = device.getName() + "-UDPController";
		this.device = device;
		this.packetSender = packetSender;
	}

	private void receive(IPPacket packet) {
		var datagram = udpExtractor.extract(packet);
		var lookupAddress = packet.getDestinationAddress().equals(device.getMulticastAddress()) ? 
				device.getMulticastAddress() : packet.getSourceAddress();
		var table = bindings.get(lookupAddress);
		if(table == null) {
			Jns3Logger.logError(name, "No socket found for IP address " + lookupAddress);
			return;
		}
		var binding = table.get(datagram.getDestinationPort());
		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + datagram.getDestinationPort());
			return;
		}
		Jns3Logger.logInfo(name, "Received datagram from " + packet.getSourceAddress() + " on port " + datagram.getDestinationPort());
		binding.socket.receiveDatagram(new DatagramPacket(packet.getSourceAddress(), 
				packet.getDestinationAddress(), datagram));
	}
	
	void sendDatagram(IPAddress boundAddress, short port, UDPDatagram datagram) {

		var table = bindings.get(boundAddress);
		var binding = table.get(port);

		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + port);
			return;
		}
		var packet = new IPPacket(IPPacket.UDP, 64,
				device.getIPAddress(), boundAddress, datagram.getBytes()); 
		packetSender.send(packet);		  
	}

	public void bind(IPAddress ip, short port, DatagramSocket socket) {
		var table = bindings.computeIfAbsent(ip, k -> new HashMap<>());
		table.put(port, new SocketBinding(ip, port, socket));
		socket.setBinding(this, port, ip);
	}

	public void unbind(DatagramSocket datagramSocket) {
		var table = bindings.get(datagramSocket.boundAddress);
		if(table != null) {
			table.remove(datagramSocket.port);
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
