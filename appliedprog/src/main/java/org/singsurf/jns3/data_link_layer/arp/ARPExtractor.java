package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;

public class ARPExtractor implements ProtocolExtractor<EthernetFrame, ARPMessage> {
	private static final ARPExtractor instance = new ARPExtractor();
	public ARPExtractor() {
	}

	@Override
	public ARPMessage extract(EthernetFrame frame) {
		return ARPMessage.fromFrame(frame);
	}

	public static ARPExtractor getInstance() {
		return instance;
	}

	@Override
	public boolean isExtractable(EthernetFrame frame) {
		return ARPMessage.isARPMessage(frame);
	}

}
