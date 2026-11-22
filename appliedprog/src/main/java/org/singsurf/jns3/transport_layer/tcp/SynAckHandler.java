package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.transport_layer.tcp.TCPProtocol.PortAddr;

public class SynAckHandler implements ProtocolHandler<TCPSegment> {
	TCPProtocol tcpProtocol;
	Random random = new Random();
	public SynAckHandler(TCPProtocol tcpProtocol) {
		super();
		this.tcpProtocol = tcpProtocol;
	}


	@Override
	public HandlerStatus handle(TCPSegment datagram) {
		if(!datagram.isSYN()) {
			return HandlerStatus.CONTINUE;
		}
		if(!datagram.isACK()) {
			return HandlerStatus.CONTINUE;
		}
		receiveSynAck(datagram);
		return HandlerStatus.COMPLETED;
	}

	
	void receiveSynAck(TCPSegment segment) {
		var portAddr = new PortAddr(
				segment.getPsudoHeader().sourceIP(), 
				segment.getSourcePort());
		var cv = tcpProtocol.clientViews.get(portAddr);
		if(cv == null) {
			Jns3Logger.logError(tcpProtocol.getName(), "No client view found for SYN-ACK from " + portAddr);
			return;
		}
		cv.receiveSynAck(segment);
	}

}
