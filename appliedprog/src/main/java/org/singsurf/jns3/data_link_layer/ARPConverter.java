package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolExtractor;

public class ARPConverter implements ProtocolExtractor<EthernetFrame, ARPMessage> {

	public ARPConverter() {
	}

	@Override
	public ARPMessage extract(EthernetFrame frame) {
		return ARPMessage.fromFrame(frame);
	}



}
