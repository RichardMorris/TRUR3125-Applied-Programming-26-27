package org.singsurf.jns3.transport_layer.tcp;

import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.network_layer.IPPacket;

public class TCPPacketHandler implements ProtocolHandler<IPPacket> {
	TCPProtocol tcpProtocol;

	public TCPPacketHandler(TCPProtocol tcpProtocol) {
		super();
		this.tcpProtocol = tcpProtocol;
	}

	@Override
	public HandlerStatus handle(IPPacket type) {
		if(type.getProtocol() != IPPacket.TCP) {
			return HandlerStatus.CONTINUE;
		}
		var segment = TCPSegment.fromPacket(type);
		tcpProtocol.delegator.receive(segment);
		return HandlerStatus.COMPLETED;
	}
}
