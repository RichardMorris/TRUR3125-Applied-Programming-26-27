package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.transport_layer.tcp.TCPProtocol.PortAddr;

public class ServerAckHandler implements ProtocolHandler<TCPSegment> {
	TCPProtocol tcpProtocol;
	Random random = new Random();
	public ServerAckHandler(TCPProtocol tcpProtocol) {
		super();
		this.tcpProtocol = tcpProtocol;
	}


	@Override
	public HandlerStatus handle(TCPSegment datagram) {
		if(datagram.getFlags() !=  TCPSegment.FLAG_ACK) {
			return HandlerStatus.CONTINUE;
		}		
		
		receiveAck(datagram);
		return HandlerStatus.COMPLETED;
	}
	
	void receiveAck(TCPSegment datagram) {
		var portAddr = new PortAddr(
				datagram.getPsudoHeader().sourceIP(), 
				datagram.getSourcePort());
		
		var sv = tcpProtocol.serverViews.get(portAddr);
		if(sv == null) {
			Jns3Logger.logError(tcpProtocol.getName(), "No client view found for SYN-ACK from " + portAddr);
			return;
		}
		sv.receiveAck(datagram);
	}


}
