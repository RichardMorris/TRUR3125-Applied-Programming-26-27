package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.physical_layer.BlockingByteSender;
import org.singsurf.jns3.physical_layer.ByteReceiver;

public class EthernetProtocol implements Protocol, LowerPhysicalAdapter, UpperPort<EthernetFrame> {

	AddressableDevice device;

	FrameDelegator delegator;
	ByteReceiver byteReceiver;
	BufferedFrameSender bufferedSender = new BufferedFrameSender();
	
	ByteToFrameConverter converter = new ByteToFrameConverter();

	public EthernetProtocol(AddressableDevice device) {
		this.device = device;
		this.delegator = new FrameDelegator(device.getName());
		delegator.addHandler(new FrameChecker(device));
		byteReceiver = new ByteReceiver() {
			@Override
			public void receive(byte[] frame) {
				EthernetFrame ethernetFrame = converter.fromBytes(frame);
				delegator.receive(ethernetFrame);
			}
		};
	}

	@Override
	public void register(LowerAdapter<EthernetFrame> adapter) {
		adapter.setOutboundSender(bufferedSender);
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
		
	public FrameReceiver getFrameReceiver() {
		return delegator;
	}
	
	public FrameDelegator getFrameDelegator() {
		return delegator;
	}
	public FrameSender getFrameSender() {
		return bufferedSender;
	}

	@Override
	public ByteReceiver getByteReceiver() {
		return byteReceiver;
	}

	@Override
	public void setByteSender(BlockingByteSender sender) {
		bufferedSender.setByteSender(sender);
	}



}
