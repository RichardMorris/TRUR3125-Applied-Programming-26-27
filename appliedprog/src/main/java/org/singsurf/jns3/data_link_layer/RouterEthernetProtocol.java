package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.MultiPortByteReceiver;

/**
 * Ethernet protocol implementation for a router with multiple ports.
 * Handles sending and receiving Ethernet frames across multiple ports.
 * Has a mapping of port to MacAddress to enable multiplexing. 
 */
public class RouterEthernetProtocol extends EthernetProtocol {
	MultiPortFrameSender frameSender;
	MultiPortFrameReceiver receiver;

	
	public RouterEthernetProtocol(ProtocolDevice device) {
		super(device);
		this.portMapper = new MultiPortMapper(numPorts);
		
		frameSender = new MultiPortFrameSender(this);
		receiver = new MultiPortFrameReceiver(this, delegator);
		
		mpr = new MultiPortByteReceiver() {
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


	@Override
	public void setByteSenders(BlockingEndPoint[] bep) {
		frameSender.setByteSenders(bep);
	}

	@Override
	public MultiPortByteReceiver getMultiByteReceiver() {
		return mpr;
	}

	@Override
	public FrameSender getFrameSender() {
		return frameSender;
	}

}
