package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;

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
	public UDPController udpController;	
	
	
	
	public DatagramSocket(String name, UDPController udpController) {
		super(name);
		this.udpController = udpController;
	}

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

	public void close() {
		udpController.unbind(this);
	}


	public void bind(IPAddress address, short boundPort) {
		port = boundPort;
		boundAddress = address;
		udpController.bind(address, boundPort, this);
	}
	
}
