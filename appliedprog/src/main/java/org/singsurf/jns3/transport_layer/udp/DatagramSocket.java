package org.singsurf.jns3.transport_layer.udp;

import java.util.ArrayDeque;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;


public class DatagramSocket {
	Queue<DatagramPacket> receiveQueue = new ArrayDeque<>();
	UDPController udpController;
	short port;
	String name;
	
	public DatagramSocket(String name) {
		this.name = name;
	}

	public void receiveDatagram(DatagramPacket datagram) {
		Jns3Logger.logInfo(name, "Received datagram from " + datagram.ip() + " on port " + datagram.datagram().getDestinationPort() +" " +datagram.datagram());
		receiveQueue.add(datagram);
	}
	
	public DatagramPacket poll() {
		return receiveQueue.poll();
	}

	void setBinding(UDPController udpController, short port) {
		this.udpController = udpController;
		this.port = port;
	}

	public void send(UDPDatagram datagram) {
		if(udpController == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to a controller");
		}
		udpController.sendDatagram(port,datagram);
	}
}
