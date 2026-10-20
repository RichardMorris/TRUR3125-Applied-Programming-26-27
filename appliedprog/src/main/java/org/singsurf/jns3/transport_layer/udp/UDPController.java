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
	
	Map<Short, SocketBinding> bindings = new HashMap<>();
	IPAddress sourceAddress;
	
	record SocketBinding(IPAddress ip, short port, DatagramSocket socket) {}
	
	record DatagramPacket(IPAddress ip, UDPDatagram datagram) {}

	public UDPController(AddressableDevice device, PacketSender packetSender,PacketReceiver nextReceiver) {
		this.name = device.getName() + "-UDPController";
		this.nextReceiver = nextReceiver;
		this.packetSender = packetSender;
		this.sourceAddress = device.getIPAddress();
	}

	@Override
	public void receivePacket(IPPacket packet) {
		if(!UDPDatagram.isUDPDatagram(packet)) {
			nextReceiver.receivePacket(packet);
			return;
		}
		var datagram = UDPDatagram.fromPacket(packet);
		var binding = bindings.get(datagram.getDestinationPort());
		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + datagram.getDestinationPort());
			return;
		}
		Jns3Logger.logInfo(name, "Received datagram from " + packet.getSourceAddress() + " on port " + datagram.getDestinationPort());
		binding.socket.receiveDatagram(new DatagramPacket(packet.getSourceAddress(), datagram));
	}
	
	public void bind(IPAddress ip, short port, DatagramSocket socket) {
		bindings.put(port, new SocketBinding(ip, port, socket));
		socket.setBinding(this, port);
	}

	public void unbind(short port) {
		bindings.remove(port);
	}

	public void sendDatagram(short port, UDPDatagram datagram) {
		var binding = bindings.get(port);

		if(binding == null) {
			Jns3Logger.logError(name, "No socket found for port " + port);
			return;
		}
		var packet = new IPPacket(IPPacket.UDP, 64,
		        sourceAddress, binding.ip, datagram.toByteArray()); 
		packetSender.sendPacket(packet);		  
	}
}
