package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.physical_layer.BlockingEndPoint;

/**
 * Sends frames to multiple ports.
 * De-multiplexes frames based on destination MAC address.
 * Buffers frames for each port to avoid blocking the sending thread.
 */
class MultiPortFrameSender implements FrameSender {
	final EthernetProtocol rep;
	private BufferedFrameSender[] senders;

	MultiPortFrameSender(EthernetProtocol routerEthernetProtocol) {
		this.rep = routerEthernetProtocol;
		senders = new BufferedFrameSender[rep.getNumPorts()];
		for(int i = 0; i < rep.getNumPorts(); i++) {
			senders[i] = new BufferedFrameSender();
		}
	}
	
	@Override
	public void send(EthernetFrame frame) {
		if(rep.getNumPorts() == 1) {
			senders[0].send(frame);
			return;
		}
		var destMac = frame.getDestination();
		var srcMac = frame.getSource();
		if(destMac.equals(MACAddress.BROADCAST_MAC)) {
			int index = rep.getPortForMacAddress(srcMac);
			for(int i = 0; i < rep.getNumPorts(); i++) {
				if(i != index) {
					senders[i].send(frame);
				}
			}
			return;
		} else {
			int index = rep.getPortForMacAddress(destMac);
			if(index != -1) {
					senders[index].send(frame);
					return;
			}
			Jns3Logger.logError(rep.getName(), "Destination MAC address " + destMac + " not found in any port mapping");
		}
	}

	public void setByteSenders(BlockingEndPoint[] bep) {
		for(int i = 0; i < rep.getNumPorts(); i++) {
			senders[i].setByteSender(bep[i]);
		}
	}	
}