package org.singsurf.jns3.data_link_layer.arp;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.AddressableDevice;

/**
 * Forwards ARP frames to all devices on the local network.
 */
class ARPForwarder implements FrameHandler {
    AddressableDevice device;
    FrameSender sender;
    
    ARPForwarder(AddressableDevice device) {
		this.device = device;
	}

	@Override
	public HandlerStatus handle(EthernetFrame frame) {
	        if(frame.getEtherType() != ARPProtocol.ARP_ETHER_TYPE) return HandlerStatus.CONTINUE;

	        if(frame.getDestination().equals(MACAddress.BROADCAST_MAC)) {
	        	sender.send(frame); // Forward the ARP frame to all devices
		        return HandlerStatus.CONTINUE; // Continue processing the ARP frame
	        }
	        if(!frame.getDestination().equals(device.getMACAddress())) {
	        	// not for this device, forward
	        	sender.send(frame);
	        	return HandlerStatus.COMPLETED;
	        }
	        return HandlerStatus.CONTINUE;
	}

	void setSender(FrameSender sender) {
		this.sender = sender;
	}
	
	
}