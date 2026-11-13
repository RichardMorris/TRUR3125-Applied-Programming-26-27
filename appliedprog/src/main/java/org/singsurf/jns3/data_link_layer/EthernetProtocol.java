package org.singsurf.jns3.data_link_layer;

import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.MultiPortByteReceiver;

public class EthernetProtocol implements Protocol, LowerPhysicalAdapter, UpperPort<EthernetFrame> {
	String name;
	ProtocolDevice device;

	PortMapper portMapper;
	ByteToFrameConverter converter = new ByteToFrameConverter();
	MultiPortByteReceiver byteReceiver;
	MultiPortFrameReceiver frameReceiver;
	FrameDelegator delegator;
	MultiPortFrameSender frameSender;

	public EthernetProtocol(ProtocolDevice device) {
		this.name = device.getName() + "-ETH";
		this.device = device;
		this.portMapper = new MultiPortMapper(
			device.getNumInternalPorts(),
			device.getNumExternalPorts());

		this.frameSender = new MultiPortFrameSender(this);
		this.delegator = new FrameDelegator(device.getName());
		this.delegator.addHandler(new FrameChecker(device));

		this.frameReceiver = new MultiPortFrameReceiver(this, (FrameReceiver) delegator);
			
		this.byteReceiver = new MultiPortByteReceiver() {
			@Override
			public void receiveFrame(int portNumber, byte[] frame) {
				var ethFrame = converter.fromBytes(frame);
				if(ethFrame == null) {
					Jns3Logger.logError(getName(), "Failed to parse Ethernet frame from bytes");
					return;
				}
				frameReceiver.receiveFrame(portNumber, ethFrame);
			}
		}; 
	}

	// Port and adapter methods
	
	@Override
	public void setByteSenders(BlockingEndPoint[] bep) {
		frameSender.setByteSenders(bep);
	}

	@Override
	public void register(LowerAdapter<EthernetFrame> adapter) {
		adapter.setOutboundSender(getFrameSender());
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
		return frameSender;
	}


	public void receive(EthernetFrame frame) {
		delegator.receive(frame);
	}


	public String getName() {
		return name;
	}

	@Override
	public MultiPortByteReceiver getMultiByteReceiver() {
		return byteReceiver;
	}
	
	void putPortMapping(int portNumber, MACAddress srcMac) {
		portMapper.putPortMapping(portNumber, srcMac);
	}

	public List<Integer> getPortsForMacAddress(MACAddress destMac, MACAddress srcMac) {
		return portMapper.getPortsForMacAddress(destMac, srcMac);
	}

}
