package org.singsurf.jns3.transport_layer.tcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.PolledSocket;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.network_layer.IPAddress;

/**
 * A socket that can be used by applications
 * to send and receive TCP datagrams through the
 * {@link TCPProtocol}. 
 * Sockets can be created by applications 
 * and bound to a port and IP address.
 */
public class TCPSocket extends PolledSocket<SocketMessage> implements NamedDevice {
	TCPProtocol tcpProtocol;
	
	
	TCPSocket(String name, TCPProtocol protocol) {
		super(name);
		this.tcpProtocol = protocol;
	}

	TCPSocket(String name, TCPProtocol tcpProtocol2, ProtocolReceiver<SocketMessage> receiver) {
		super(name, receiver);
		this.tcpProtocol = tcpProtocol2;
	}

	@Override 
	public String toString() {
		return "DatagramSocket["+name+" " + getAddress() + ":" + port + "]";
	}

	public void send(SocketMessage message) {
		if(tcpProtocol == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to a controller");
		}
		if(address == null) {
			Jns3Logger.logError("DatagramSocket", "Socket is not bound to an address");
		}
		tcpProtocol.transmitMessage(message);
	}

	/**
	 * Closes the socket and unbinds it from the {@link TCPProtocol}.
	 */
	@Override
	public void close() {
		tcpProtocol.unbind(this);
	}

	/**
	 * Binds the socket to a specific IP address and port number.
	 * @param ip
	 * @param boundPort
	 */
	@Override
	public void bind(IPAddress ip, short boundPort) {
		port = boundPort;
		address = ip;
		tcpProtocol.bind(ip, boundPort, this);
	}
	
}
