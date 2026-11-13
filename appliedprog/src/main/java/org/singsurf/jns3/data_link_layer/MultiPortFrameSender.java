package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;

/**
 * Sends frames to multiple ports.
 * De-multiplexes frames based on destination MAC address.
 * Buffers frames for each port to avoid blocking the sending thread.
 */
class MultiPortFrameSender implements FrameSender {
	EthernetProtocol ethernetProtocol;
	BufferedFrameSender[] senders;

	MultiPortFrameSender(EthernetProtocol routerEthernetProtocol) {
		this.ethernetProtocol = routerEthernetProtocol;
		senders = new BufferedFrameSender[ethernetProtocol.device.getNumPorts()];
		for(int i = 0; i < ethernetProtocol.device.getNumPorts(); i++) {
			senders[i] = new BufferedFrameSender();
		}
	}
	
	@Override
	public void send(EthernetFrame frame) {
		if(ethernetProtocol.device.getNumPorts() == 1) {
			senders[0].send(frame);
			return;
		}
		var destMac = frame.getDestination();
		var srcMac = frame.getSource();
		var indicies = ethernetProtocol.getPortsForMacAddress(destMac,srcMac);
		
		if(indicies.size() == 0) {
			Jns3Logger.logError(ethernetProtocol.getName(), "Destination MAC address " + destMac + " not found in any port mapping");
		}
		for(int i : indicies) {
			senders[i].send(frame);
		}
	}

	public void setByteSenders(BlockingEndPoint[] bep) {
		for(int i = 0; i < ethernetProtocol.device.getNumPorts(); i++) {
			senders[i].setByteSender(bep[i]);
		}
	}	
}