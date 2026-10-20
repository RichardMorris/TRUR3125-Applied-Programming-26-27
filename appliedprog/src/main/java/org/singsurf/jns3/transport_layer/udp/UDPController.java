package org.singsurf.jns3.transport_layer.udp;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketReceiver;
import org.singsurf.jns3.network_layer.PacketSender;

public class UDPController implements PacketReceiver {
	PacketReceiver nextReceiver;
	PacketSender packetSender;
	String name;
	AddressableDevice device;
	Map<IPAddress,Map<Short, SocketBinding>> bindings = new HashMap<>();
	private IPAddress multicastAddress;
	
	record SocketBinding(IPAddress ip, short port, DatagramSocket socket) {}
	
	record DatagramPacket(IPAddress ip, IPAddress dest, UDPDatagram datagram) {}

	public UDPController(AddressableDevice device, PacketSender packetSender,PacketReceiver nextReceiver) {
		this.name = device.getName() + "-UDPController";
		this.device = device;
		this.nextReceiver = nextReceiver;
		this.packetSender = packetSender;
	}

	@Override
	public void receivePacket(IPPacket packet) {
		if(!UDPDatagram.isUDPDatagram(packet)) {
			nextReceiver.receivePacket(packet);
			return;
		}
		var datagram = UDPDatagram.fromPacket(packet);
		var lookupAddress = packet.getDestinationAddress().equals(multicastAddress) ? 
				multicastAddress : packet.getSourceAddress();
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
	
	public void bind(IPAddress ip, short port, DatagramSocket socket) {
		var table = bindings.computeIfAbsent(ip, k -> new HashMap<>());
		table.put(port, new SocketBinding(ip, port, socket));
		socket.setBinding(this, port, ip);
	}

	public void sendDatagram(short port, IPAddress boundAddress, UDPDatagram datagram) {

		var table = bindings.get(boundAddress);
		var binding = table.get(port);

		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + port);
			return;
		}
		var packet = new IPPacket(IPPacket.UDP, 64,
				device.getIPAddress(), boundAddress, datagram.toByteArray()); 
		packetSender.sendPacket(packet);		  
	}

	public void setMulticastAddress(IPAddress multiCastIP) {
		this.multicastAddress = multiCastIP;
	}	
}
