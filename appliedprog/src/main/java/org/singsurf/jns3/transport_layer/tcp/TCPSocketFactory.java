package org.singsurf.jns3.transport_layer.tcp;

import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSocket;
import org.singsurf.jns3.generic_protocol.SocketFactory;

public class TCPSocketFactory implements SocketFactory<InternetMessage> {
	TCPProtocol tcpProtocol;

	TCPSocketFactory(TCPProtocol protocol) {
		this.tcpProtocol = protocol;
	}

	@Override
	public ProtocolSocket<InternetMessage> createSocket(String name) {
		return new TCPSocket(name, tcpProtocol);
	}

	@Override
	public ProtocolSocket<InternetMessage> createSocket(String name, ProtocolReceiver<InternetMessage> receiver) {
		return new TCPSocket(name, tcpProtocol, receiver);
	}

}
