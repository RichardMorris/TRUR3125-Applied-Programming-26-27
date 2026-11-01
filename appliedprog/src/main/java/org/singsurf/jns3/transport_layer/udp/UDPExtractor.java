package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram.PsudoHeader;

public class UDPExtractor implements ProtocolExtractor<IPPacket, UDPDatagram> {
	private static final UDPExtractor instance = new UDPExtractor();
	public UDPExtractor() {
	}


	public static UDPExtractor getInstance() {
		return instance;
	} 


	@Override
	public UDPDatagram extract(IPPacket packet) {
		var psudo = new PsudoHeader(packet.getSourceAddress(), packet.getDestinationAddress(), IPPacket.UDP);
		return UDPDatagram.fromBytes(packet.getContents(),psudo);
	}


	@Override
	public boolean isExtractable(IPPacket type) {
		byte protocol = type.getProtocol();
		return protocol == IPPacket.UDP; // 17 is the protocol number for UDP
	}

}
