package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.generic_protocol.LowerPhysicalAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.UpperPhysicalPort;
import org.singsurf.jns3.hardware.AddressableDevice;

public class PhysicalProtocol implements Protocol, UpperPhysicalPort {
	AddressableDevice device;
	BlockingEndPoint bep;

	public PhysicalProtocol(AddressableDevice device) {
		super();
		this.device = device;
        this.bep = new BlockingEndPoint(device.getName()); 

	}

	@Override
	public void register(LowerPhysicalAdapter adapter) {
		adapter.setByteSender(bep);
		bep.setByteReceiver(adapter.getByteReceiver());
	}

	public BlockingEndPoint getEndPoint() {
		return bep;
	}


}
