package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.PolledSocket;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;

/**
 * A socket that can be used by applications
 * to send and receive UDP datagrams through the
 * {@link UDPController}. 
 * Sockets can be created by applications 
 * and bound to a port and IP address.
 */
public class DatagramSocket extends PolledSocket<UDPDatagram> implements NamedDevice {
	public UDPController udpController;	
	
	
	
	DatagramSocket(String name, UDPController udpController) {
		super(name);
		this.udpController = udpController;
	}

	public DatagramSocket(String name, UDPController udpController, 
			ProtocolReceiver<UDPDatagram> receiver) {
		super(name, receiver);
		this.udpController = udpController;
	}

	@Override 
	public String toString() {
		return "DatagramSocket ["+name+" " + getAddress() + ":" + port + "]";
	}

	public void send(UDPDatagram datagram) {
		if(udpController == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to a controller");
		}
		if(address == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to an address");
		}
		udpController.sendDatagram(address,port,datagram);
	}

	/**
	 * Closes the socket and unbinds it from the {@link UDPController}.
	 */
	@Override
	public void close() {
		udpController.unbind(this);
	}

	/**
	 * Binds the socket to a specific IP address and port number.
	 * @param address
	 * @param boundPort
	 */
	@Override
	public void bind(IPAddress ip, short boundPort) {
		port = boundPort;
		address = ip;
		udpController.bind(ip, boundPort, this);
	}
	
}
