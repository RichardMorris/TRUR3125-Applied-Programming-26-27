package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ByteConverter;

/**
 * A converter between EthernetFrame and byte[].
 */
public class ByteToFrameConverter implements ByteConverter<EthernetFrame> {

	@Override
	public EthernetFrame fromBytes(byte[] bytes) {
		return EthernetFrame.parseBytes(bytes);
	}

	@Override
	public byte[] toBytes(EthernetFrame frame) {
		return frame.getBytes();
	}

}
