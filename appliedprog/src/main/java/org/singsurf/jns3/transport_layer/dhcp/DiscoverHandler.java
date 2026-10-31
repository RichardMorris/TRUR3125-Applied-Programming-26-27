package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;

public class DiscoverHandler implements ProtocolHandler<DHCPMessage> {

	public DiscoverHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPProtocol.DHCP_OP_DISCOVER) {
			return HandlerStatus.CONTINUE;
		}
		Jns3Logger.logInfo("DiscoverHandler", "Received DHCP Discover message");
		return HandlerStatus.COMPLETED;
	}

}
