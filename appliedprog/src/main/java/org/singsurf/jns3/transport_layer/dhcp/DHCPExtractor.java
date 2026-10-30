package org.singsurf.jns3.transport_layer.dhcp;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

public class DHCPExtractor implements ProtocolExtractor<UDPDatagram, DHCPMessage> {

	public DHCPExtractor() {
	}

	@Override
	public DHCPMessage extract(UDPDatagram type) {
		return new DHCPMessage(type.getContents());
	}

	@Override
	public boolean isExtractable(UDPDatagram type) {
		if(type.getDestinationPort() == DHCPProtocol.DHCP_SERVER_PORT 
				|| type.getDestinationPort() == DHCPProtocol.DHCP_CLIENT_PORT) {
			return true;
		}
		return false;
	}

}
