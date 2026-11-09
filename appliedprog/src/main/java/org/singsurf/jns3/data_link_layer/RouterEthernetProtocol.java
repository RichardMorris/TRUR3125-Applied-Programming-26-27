package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.MultiPortReceiver;

/**
 * Ethernet protocol implementation for a router with multiple ports.
 * Handles sending and receiving Ethernet frames across multiple ports.
 * Has a mapping of port to MacAddress to enable multiplexing. 
 */
public class RouterEthernetProtocol extends EthernetProtocol {
	MultiPortFrameSender frameSender;
	MultiPortFrameReceiver receiver;
	private int numPorts;

	MultiPortReceiver mpr = new MultiPortReceiver() {
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
	
	public RouterEthernetProtocol(ProtocolDevice device) {
		super(device);
		numPorts = device.getNumPorts();
		this.portMapper = new MultiPortMapper(numPorts);
		
		frameSender = new MultiPortFrameSender(this);
		receiver = new MultiPortFrameReceiver(this, delegator);
	}

	public int getNumPorts() {
		return numPorts;
	}

	@Override
	public void setByteSenders(BlockingEndPoint[] bep) {
		frameSender.setByteSenders(bep);
	}

	@Override
	public MultiPortReceiver getMultiByteReceiver() {
		return mpr;
	}

	@Override
	public FrameSender getFrameSender() {
		return frameSender;
	}

}
