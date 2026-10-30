package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.generic_protocol.SocketFactory;

public class UDPSocketFactory implements SocketFactory<UDPDatagram> {
	UDPController udpController;
	

	
	public UDPSocketFactory(UDPProtocol udpProtocol) {
		super();
		this.udpController = udpProtocol.udpController;
	}



	@Override
	public DatagramSocket createSocket(String name) {
		return new DatagramSocket(name, udpController);
	}

}
