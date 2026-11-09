package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.ByteReceiver;
import org.singsurf.jns3.physical_layer.MultiPortByteReceiver;

public class EthernetProtocol implements Protocol, LowerPhysicalAdapter, UpperPort<EthernetFrame> {
	String name;
	ProtocolDevice device;

	ProtocolDelegator<EthernetFrame> delegator;
	ByteToFrameConverter converter = new ByteToFrameConverter();

	MultiPortByteReceiver byteReceiver;
	MultiPortFrameSender frameSender; // = new BufferedFrameSender();	
	PortMapper portMapper;
	int numPorts;

	public EthernetProtocol(ProtocolDevice device) {
		this.name = device.getName() + "-ETH";
		this.device = device;
		numPorts = device.getNumPorts();
		this.portMapper = new MultiPortMapper(numPorts);

		frameSender = new MultiPortFrameSender(this);
		this.delegator = new ProtocolDelegator<EthernetFrame>(device.getName());
		delegator.addHandler(new FrameChecker(device));

		if(numPorts == 1) {
			var byteReceiver = new ByteReceiver() {
				@Override
				public void receive(byte[] frame) {
					EthernetFrame ethernetFrame = converter.fromBytes(frame);
					delegator.receive(ethernetFrame);
				}
			};	
			this.byteReceiver = new MultiPortByteReceiver() {
				@Override
				public void receiveFrame(int portNumber, byte[] frame) {
					byteReceiver.receive(frame);
				}
			};
		} else {
			var receiver = new MultiPortFrameReceiver(this, delegator);
			
			byteReceiver = new MultiPortByteReceiver() {
				@Override
				public void receiveFrame(int portNumber, byte[] frame) {
					var ethFrame = converter.fromBytes(frame);
					if(ethFrame == null) {
						Jns3Logger.logError(getName(), "Failed to parse Ethernet frame from bytes");
						return;
					}
					receiver.receiveFrame(portNumber, ethFrame);
				}
			}; 

			
		}

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
	
	int getPortForMacAddress(MACAddress destMac) {
		return portMapper.getPortMapping(destMac);
	}

	void putPortMapping(int portNumber, MACAddress srcMac) {
		portMapper.putPortMapping(portNumber, srcMac);
	}

	public int getNumPorts() {
		return numPorts;
	}

}
