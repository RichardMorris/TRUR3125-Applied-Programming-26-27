package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

public class EthernetProtocol implements UpperPort<EthernetFrame> {

	AddressableDevice device;

	FrameDelegator delegator;
	FrameSender sender;
	PhysicalEndPoint endPoint;
	
	public EthernetProtocol(AddressableDevice device) {
		this.device = device;
		this.delegator = new FrameDelegator(device.getName());
		var nic = new NICCard(device.getName(), delegator);
		this.sender = nic;
		this.endPoint = nic;
		delegator.addHandler(new FrameChecker(device));
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

	public PhysicalEndPoint getEndPoint() {
		return endPoint;
	}
}
