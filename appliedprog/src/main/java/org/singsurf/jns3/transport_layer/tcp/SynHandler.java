package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;

public class SynHandler implements ProtocolHandler<TCPSegment> {
	TCPProtocol tcpProtocol;
	Random random = new Random();
	public SynHandler(TCPProtocol tcpProtocol) {
		super();
		this.tcpProtocol = tcpProtocol;
	}


	@Override
	public HandlerStatus handle(TCPSegment datagram) {
		if(!datagram.isSYN()) {
			return HandlerStatus.CONTINUE;
		}
		if(datagram.isACK()) {
			return HandlerStatus.CONTINUE;
		}
		tcpProtocol.receiveSyn(datagram);
		return HandlerStatus.COMPLETED;
	}

}
