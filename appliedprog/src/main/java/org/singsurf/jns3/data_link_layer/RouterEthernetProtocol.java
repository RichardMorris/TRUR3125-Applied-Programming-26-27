package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.AddressableDevice;

public class RouterEthernetProtocol implements Protocol, UpperPort<EthernetFrame> {

	AddressableDevice device;

	FrameDelegator delegator;
	FrameSender sender;
	
	public RouterEthernetProtocol(AddressableDevice device, FrameSender sender) {
		this.device = device;
		this.delegator = new FrameDelegator(device.getName());
		delegator.addHandler(new FrameChecker(device));
		this.sender = sender;
	}

	@Override
	public void register(LowerAdapter<EthernetFrame> adapter) {
		adapter.setOutboundSender(sender);
		adapter.setInboundCallback(delegator);
		for(var h: adapter.getInboundHandlers()) {
			delegator.addHandler(h);
		}
	}

	@Override
	public void finalise() {
		delegator.addHandler(new DefaultFrameHandler(device));
	}
	
	public void addHandler(ProtocolHandler<EthernetFrame> handler) {
		delegator.addHandler(handler);
	}
	
	public FrameSender getFrameSender() {
		return sender;
	}
	
	public FrameReceiver getFrameReceiver() {
		return delegator;
	}
	
	public FrameDelegator getFrameDelegator() {
		return delegator;
	}

}
