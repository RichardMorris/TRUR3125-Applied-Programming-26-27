package org.singsurf.jns3.transport_layer.tcp;

import java.util.Random;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.transport_layer.tcp.TCPProtocol.PortAddr;

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
		
		
		receiveSyn(datagram);
		return HandlerStatus.COMPLETED;
	}
	
	void receiveSyn(TCPSegment datagram) {
		var portAddr = new PortAddr(
				datagram.getPsudoHeader().sourceIP(), 
				datagram.getSourcePort());
		
		var ent = tcpProtocol.serverViews.get(portAddr);
		if(ent != null) {
			Jns3Logger.logError(tcpProtocol.getName(), "Dupe SYN received for " + portAddr);
			return;
		}

		var serverView = new ServerView(tcpProtocol, datagram);
		tcpProtocol.serverViews.put(portAddr, serverView);
		serverView.sendSynAck();
	}


}
