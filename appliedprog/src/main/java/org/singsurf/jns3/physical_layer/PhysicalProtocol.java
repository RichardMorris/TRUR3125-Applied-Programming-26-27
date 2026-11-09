package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.UpperPhysicalPort;
import org.singsurf.jns3.hardware.ProtocolDevice;

public class PhysicalProtocol implements Protocol, UpperPhysicalPort {
	ProtocolDevice device;
	int numPorts;
	MultiPortByteReceiver byteReceiver;
	BlockingEndPoint[] endPoints;

	public PhysicalProtocol(ProtocolDevice device, int numPorts) {
		super();
		this.device = device;
		this.numPorts = numPorts;
        this.endPoints = new BlockingEndPoint[numPorts];
        for(int i=0; i<numPorts; i++) {
        		endPoints[i] = new BlockingEndPoint(device.getName()); 
        }
	}

	@Override
	public void register(LowerPhysicalAdapter adapter) {
			adapter.setByteSenders(endPoints);
			byteReceiver =adapter.getMultiByteReceiver();
	        for(int i=0; i<numPorts; i++) {
	        	int j=i;
	        		endPoints[i].setByteReceiver(new ByteReceiver() {
						@Override
						public void receive(byte[] frame) {
							byteReceiver.receiveFrame(j, frame);
						}
	        			
	        		});
	        }
	}

	public PhysicalEndPoint getEndPoint(int i) {
		return endPoints[i];
	}


}
