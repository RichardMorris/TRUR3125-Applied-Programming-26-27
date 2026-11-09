package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.ByteReceiver;
import org.singsurf.jns3.physical_layer.MultiPortReceiver;

public class EthernetProtocol implements Protocol, LowerPhysicalAdapter, UpperPort<EthernetFrame> {
	String name;
	ProtocolDevice device;

	ProtocolDelegator<EthernetFrame> delegator;
	ByteReceiver byteReceiver;
	BufferedFrameSender bufferedSender = new BufferedFrameSender();	
	ByteToFrameConverter converter = new ByteToFrameConverter();

	public EthernetProtocol(ProtocolDevice device) {
		this.name = device.getName() + "-ETH";
		this.device = device;
		this.delegator = new ProtocolDelegator<EthernetFrame>(device.getName());
		delegator.addHandler(new FrameChecker(device));
		byteReceiver = new ByteReceiver() {
			@Override
			public void receive(byte[] frame) {
				EthernetFrame ethernetFrame = converter.fromBytes(frame);
				delegator.receive(ethernetFrame);
			}
		};
	}

	// Port and adapter methods
	
	@Override
	public void setByteSenders(BlockingEndPoint[] bep) {
		bufferedSender.setByteSender(bep[0]);		
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
		
	// Getters for the various components of the protocol
	
	public ProtocolDelegator<EthernetFrame> getFrameReceiver() {
		return delegator;
	}
	
	public FrameSender getFrameSender() {
		return bufferedSender;
	}


	public void receive(EthernetFrame frame) {
		delegator.receive(frame);
	}


	public String getName() {
		return name;
	}

	@Override
	public MultiPortReceiver getMultiByteReceiver() {
		return new MultiPortReceiver() {
			@Override
			public void receiveFrame(int portNumber, byte[] frame) {
				byteReceiver.receive(frame);
			}
		};
	}

	public void send(EthernetFrame type) {
		bufferedSender.send(type);		
	}
}
