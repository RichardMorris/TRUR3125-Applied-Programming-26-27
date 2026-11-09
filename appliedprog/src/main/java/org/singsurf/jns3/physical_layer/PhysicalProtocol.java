package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.UpperPhysicalPort;
import org.singsurf.jns3.hardware.AddressableDevice;

public class PhysicalProtocol implements Protocol, UpperPhysicalPort {
	AddressableDevice device;
	BlockingEndPoint[] bep;
	private int numPorts;
	private MultiPortByteReceiver byteReceiver;

	public PhysicalProtocol(AddressableDevice device, int numPorts) {
		super();
		this.device = device;
		this.numPorts = numPorts;
        this.bep = new BlockingEndPoint[numPorts];
        for(int i=0; i<numPorts; i++) {
        		bep[i] = new BlockingEndPoint(device.getName()); 
        }
	}

	@Override
	public void register(LowerPhysicalAdapter adapter) {
			adapter.setByteSenders(bep);
			byteReceiver =adapter.getMultiByteReceiver();
	        for(int i=0; i<numPorts; i++) {
	        	int j=i;
	        		bep[i].setByteReceiver(new ByteReceiver() {
						@Override
						public void receive(byte[] frame) {
							byteReceiver.receiveFrame(j, frame);
						}
	        			
	        		});
	        }
	}

	public BlockingEndPoint getEndPoint(int i) {
		return bep[i];
	}


}
