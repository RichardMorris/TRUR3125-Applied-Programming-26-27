package org.singsurf.jns3.transport_layer.udp;

import java.util.ArrayDeque;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;


public class DatagramSocket implements NamedDevice {
	String name;
	short port;
	IPAddress boundAddress;
	UDPController udpController;	
	Queue<DatagramPacket> queue = new ArrayDeque<>();

	/** for debugging, log received messages */
	public boolean logMessage = false;
	
	public DatagramSocket(String name) {
		this.name = name;
	}
	
	public DatagramPacket poll() {
		return queue.poll();
	}


	public void send(UDPDatagram datagram) {
		if(udpController == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to a controller");
		}
		udpController.sendDatagram(boundAddress,port,datagram);
	}
	
	public String getName() {
		return name;
	}

	public Object getAddress() {
		return boundAddress;
	}

	public short getPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return "DatagramSocket ["+name+" " + boundAddress + ":" + port + "]";
	}

	void receiveDatagram(DatagramPacket datagram) {
		var len = Math.min(datagram.toString().length(), 200);
		if(logMessage)
			Jns3Logger.logInfo(name, "Received datagram " + datagram.toString().substring(0, len));
		queue.add(datagram);
	}

	void setBinding(UDPController udpController, short port, IPAddress ip) {
		this.udpController = udpController;
		this.port = port;
		this.boundAddress = ip;
	}

	public void close() {
		udpController.unbind(this);
	}
	
}
