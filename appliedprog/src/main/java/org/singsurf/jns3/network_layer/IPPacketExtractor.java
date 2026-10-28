package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.generic_protocol.ProtocolExtractor;

public class IPPacketExtractor implements ProtocolExtractor<EthernetFrame, IPPacket> {
	public static final IPPacketExtractor INSTANCE = new IPPacketExtractor();
	public IPPacketExtractor() {
	}

	@Override
	public IPPacket extract(EthernetFrame type) {
		return IPPacket.fromFrame(type);
	}

	public static IPPacketExtractor getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isExtractable(EthernetFrame frame) {
		return IPPacket.isIPPacket(frame.getContents());
	}

}
