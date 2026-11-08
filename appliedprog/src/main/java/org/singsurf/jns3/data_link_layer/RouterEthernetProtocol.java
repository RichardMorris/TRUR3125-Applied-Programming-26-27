package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.hardware.HomeRouter;

public class RouterEthernetProtocol extends EthernetProtocol {
	HomeRouter hr;
	HRFrameSender frameSender;

	public RouterEthernetProtocol(HomeRouter device) {
		super(device);
		this.hr = device;
		frameSender = new HRFrameSender(device);
		hr.setFrameReceiver(delegator);
	}

	@Override
	public void register(LowerAdapter<EthernetFrame> adapter) {
		adapter.setOutboundSender(frameSender);
		adapter.setInboundCallback(delegator);
		for(var h: adapter.getInboundHandlers()) {
			delegator.addHandler(h);
		}
	}

	public void send(EthernetFrame type) {
		frameSender.send(type);		
	}

}
