package org.singsurf.jns3.application_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

/**
 * A simple chat application that allows a PC to send and receive messages 
 * over public and private channels.
 * Out going messages and incoming messages are echoed to the PC's terminal.
 * 
 * See <a href="../../../../../../Week12.md">Week 12</a>.
 */
public class ChatApplication implements TickListener{
	static final short PUBLIC_PORT = 42;
	static final short PRIVATE_PORT = 43;

	PC pc;
	boolean isPrivate = false;
	private List<DatagramSocket> sockets = new ArrayList<>();
	
	/**
	 * Start the application in public mode.
	 * @param pc Host PC
	 */
	public ChatApplication(PC pc) {
		this.pc = pc;
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		var multicastAddress = pc.getMulticastAddress();
		pc.bind(socket, multicastAddress, PUBLIC_PORT);
		sockets.add(socket);
		GlobalWallClock.getInstance().addTickListener(this);
	}

	/** Start the application, in private mode to a given IP address.
	 * 
	 * @param pc Host PC
	 * @param ipAddress IP address to chat with.
	 */
	public ChatApplication(PC pc, IPAddress ipAddress) {
		this(pc);
		isPrivate = true;
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		pc.bind(socket, ipAddress, PRIVATE_PORT);
		sockets.add(socket);
	}

	/**
	 * Add a given host to a private chat.
	 * @param ipAddress 
	 */
	public void addHost(IPAddress ipAddress) {
		var socket = new DatagramSocket(pc.getName() + "-Chat");
		pc.bind(socket, ipAddress, PRIVATE_PORT);
		sockets.add(socket);
	}

	/**
	 * Remove a host from a private chat.
	 * @param ipAddress
	 */
	public void removeHost(IPAddress ipAddress) {
		for(DatagramSocket socket : sockets) {
			if(socket.getPort() == PRIVATE_PORT && socket.getAddress().equals(ipAddress)) {
				sockets.remove(socket);
				socket.close();
			}
		}		
	}

	/**
	 * Sends a message, echoing it on the terminal.
	 * @param string
	 */
	public void say(String string) {
		say(string, true);
		
	}

	/**
	 * Sends a message without echoing it on the terminal.
	 * @param string
	 */
	public void wisper(String string) {
		say(string, false);
		
	}

	/**
	 * Sends a public message in a private chat.
	 * @param message
	 */
	public void sayPublic(String message) {
		pc.display(pc.getName() + " says: " + message);
		var socket = sockets.get(0);
		socket.send(new UDPDatagram(PUBLIC_PORT, PUBLIC_PORT, message.getBytes()));
	}

	private void say(String message, boolean echo) {
		if(!isPrivate) {
			sayPublic(message);
			return;
		}
		if(echo)
			pc.display(pc.getName() + " says: " + message);
		for(DatagramSocket socket : sockets) {
			if(socket.getPort() == PRIVATE_PORT) {
//				Jns3Logger.logInfo("ChatApplication", "Sending private message to " + socket);
				socket.send(new UDPDatagram(PRIVATE_PORT, PRIVATE_PORT, message.getBytes()));
			}
		}
	}

	/**
	 * On every clock tick the system polls for incoming
	 * messages and sends them on the terminal.
	 */
	@Override
	public void onTick() {
		DatagramPacket datagram;
		for(DatagramSocket socket : sockets) {
			while((datagram = socket.poll()) != null) {
				String message = new String(datagram.datagram().getContents());
				pc.display(pc.getName()+ ": " +  datagram.source() + " says: " + message);
			}
		}
	}
	
}
