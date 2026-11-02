package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;

public class AckHandler implements ProtocolHandler<DHCPMessage> {
	DHCPProtocol dhcpProtocol;
	AddressableDevice device;
	String name;
	
	public AckHandler(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
		this.device = dhcpProtocol.device;
		this.name = dhcpProtocol.device.getName() + "-DHCP-Ack";
	}
	
	@Override
	public HandlerStatus handle(DHCPMessage message) {
		if(message.op != DHCPMessage.DHCP_OP_OFFER) {
			return HandlerStatus.CONTINUE;
		}
		var op = message.getOption(DHCPMessage.OPT_MSG_TYPE);
		if(op == null || op.data()[0] != DHCPMessage.DHCP_OP_ACK) {
			return HandlerStatus.CONTINUE;
		}
		Jns3Logger.logInfo(name, "Received DHCP Ack message YIADDR "+ message.getYiaddr().toString());

		dhcpProtocol.device.setIPAddress(message.getYiaddr());
		return HandlerStatus.COMPLETED;
	}

}
