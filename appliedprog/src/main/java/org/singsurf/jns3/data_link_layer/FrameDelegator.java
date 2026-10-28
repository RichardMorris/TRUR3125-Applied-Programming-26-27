package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolDelegator;

public class FrameDelegator extends ProtocolDelegator<EthernetFrame> implements FrameReceiver {

	public FrameDelegator(String name) {
		super(name);
	}
	
}