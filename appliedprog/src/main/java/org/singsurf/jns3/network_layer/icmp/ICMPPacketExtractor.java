package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;
import org.singsurf.jns3.network_layer.IPPacket;

public class ICMPPacketExtractor implements ProtocolExtractor<IPPacket, ICMPMessage> {
	public static final ICMPPacketExtractor instance = new ICMPPacketExtractor();
	public ICMPPacketExtractor() {
	}

	@Override
	public ICMPMessage extract(IPPacket type) {
		return ICMPMessage.parsePacket(type);
	}

	public static ICMPPacketExtractor getInstance() {
		return instance;
	}

	@Override
	public boolean isExtractable(IPPacket packet) {
		return ICMPMessage.isICMPMessage(packet);
	}
}
