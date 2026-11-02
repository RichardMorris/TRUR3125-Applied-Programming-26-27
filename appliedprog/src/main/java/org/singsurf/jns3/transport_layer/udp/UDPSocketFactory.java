package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.SocketFactory;

/**
 * A factory for creating {@link DatagramSocket} instances.
 */
public class UDPSocketFactory implements SocketFactory<UDPDatagram> {
	UDPController udpController;
	
	UDPSocketFactory(UDPProtocol udpProtocol) {
		super();
		this.udpController = udpProtocol.udpController;
	}

	@Override
	public DatagramSocket createSocket(String name) {
		return new DatagramSocket(name, udpController);
	}

	@Override
	public DatagramSocket createSocket(String name, ProtocolReceiver<UDPDatagram> receiver) {
		return new DatagramSocket(name, udpController, receiver);
	}

}
