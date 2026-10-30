package org.singsurf.jns3.transport_layer.udp;

import java.util.ArrayDeque;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;

/**
 * A socket that can be used by applications
 * to send and receive UDP datagrams through the
 * {@link UDPController}. 
 * Sockets can be created by applications 
 * and bound to a port and IP address using the
 * {@link org.singsurf.jns3.hardware.PC#bind(DatagramSocket, IPAddress, short) PC.bind(DatagramSocket, IPAddress, portNumber)} method.
 * 
 */
public class DatagramSocket extends ProtocolSocket<UDPDatagram> implements NamedDevice {
	public DatagramSocket(String name) {
		super(name);
	}
	
	protected Queue<DatagramPacket> queue = new ArrayDeque<>();


	public UDPController udpController;	
	/** for debugging, log received messages */
	public boolean logMessage = false;
	
	
	@Override 
	public String toString() {
		return "DatagramSocket ["+name+" " + getBoundAddress() + ":" + port + "]";
	}

	public void send(UDPDatagram datagram) {
		if(udpController == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to a controller");
		}
		udpController.sendDatagram(boundAddress,port,datagram);
	}

	public void receiveDatagram(DatagramPacket datagramPacket) {
		var len = Math.min(datagramPacket.toString().length(), 200);
		if(LOG_MESSAGES)
			Jns3Logger.logInfo(name, "Received datagram " + datagramPacket.toString().substring(0, len));
		queue.add(datagramPacket);
	}


	public DatagramPacket poll() {
		return queue.poll();
	}

	void setBinding(UDPController udpController, short port, IPAddress ip) {
		this.udpController = udpController;
		this.port = port;
		this.setBoundAddress(ip);
	}

	public void close() {
		udpController.unbind(this);
	}
	
}
