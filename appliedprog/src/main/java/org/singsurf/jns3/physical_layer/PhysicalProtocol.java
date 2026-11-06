package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.UpperPhysicalPort;
import org.singsurf.jns3.hardware.AddressableDevice;

public abstract class PhysicalProtocol implements Protocol, UpperPhysicalPort {
	BlockingByteSender byteSender;
	AddressableDevice device;
	
	public PhysicalProtocol(AddressableDevice device, BlockingByteSender byteSender) {
		super();
		this.device = device;
		this.byteSender = byteSender;
	}



}
