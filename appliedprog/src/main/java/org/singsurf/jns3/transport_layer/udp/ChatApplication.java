package org.singsurf.jns3.transport_layer.udp;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;

public class ChatApplication implements TickListener{
	PC pc;
	private List<DatagramSocket> sockets = new ArrayList<>();
	static final short PORT = 42;
	static final short PRIVATE_PORT = 43;
	boolean isPrivate = false;
	
	public ChatApplication(PC pc) {
		this.pc = pc;
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		var multicastAddress = pc.getMulticastAddress();
		pc.bind(socket, multicastAddress, PORT);
		sockets.add(socket);
		GlobalWallClock.getInstance().addTickListener(this);
	}

	public ChatApplication(PC pc, IPAddress ipAddress) {
		this(pc);
		isPrivate = true;
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		pc.bind(socket, ipAddress, PRIVATE_PORT);
		sockets.add(socket);
	}
	
	public void addHost(IPAddress ipAddress) {
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		pc.bind(socket, ipAddress, PRIVATE_PORT);
		sockets.add(socket);
	}


	public void say(String message) {
		if(!isPrivate) {
			sayPublic(message);
			return;
		}
		pc.display(pc.getName() + " says: " + message);
		for(DatagramSocket socket : sockets) {
			if(socket.getPort() == PRIVATE_PORT) {
//				Jns3Logger.logInfo("ChatApplication", "Sending private message to " + socket);
				socket.send(new UDPDatagram(PRIVATE_PORT, PRIVATE_PORT, message.getBytes()));
			}
		}
	}

	public void sayPublic(String message) {
		pc.display(pc.getName() + " says: " + message);
		var socket = sockets.get(0);
		socket.send(new UDPDatagram(PORT, PORT, message.getBytes()));
	}

	@Override
	public void onTick() {
		DatagramPacket datagram;
		for(DatagramSocket socket : sockets) {
			while((datagram = socket.poll()) != null) {
				String message = new String(datagram.datagram().getContents());
				pc.display(pc.getName()+ ": " +  datagram.ip() + " says: " + message);
			}
		}
	}

	
	
}
