package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;
import org.singsurf.jns3.network_layer.IPPacket;

public class UDPExtractor implements ProtocolExtractor<IPPacket, UDPDatagram> {
	private static final UDPExtractor instance = new UDPExtractor();
	public UDPExtractor() {
	}


	public static UDPExtractor getInstance() {
		return instance;
	}


	@Override
	public UDPDatagram extract(IPPacket packet) {
		return UDPDatagram.fromPacket(packet);
	}


	@Override
	public boolean isExtractable(IPPacket type) {
		return UDPDatagram.isUDPDatagram(type);
	}

}
