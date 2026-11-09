package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;

/**
 * Receives frames from multiple ports.
 * Updates the port mapping based on the source MAC address of received frames.
 */
class MultiPortFrameReceiver {
	ProtocolReceiver<EthernetFrame> frameReceiver;
	RouterEthernetProtocol routerEthernetProtocol;
	private String name;

	public MultiPortFrameReceiver(RouterEthernetProtocol rep, ProtocolReceiver<EthernetFrame> delegator) {
		this.name = rep.getName();
		this.routerEthernetProtocol = rep;
		this.frameReceiver = delegator;
	}

	public void receiveFrame(int portNumber, EthernetFrame frame) {
		var srcMac = frame.getSource();
		if(srcMac.equals(MACAddress.BROADCAST_MAC)) {
			Jns3Logger.logError(name, "Received frame with broadcast source MAC address on port " + portNumber);
			return;
		}
		if(srcMac.equals(MACAddress.ZERO_MAC)) {
			Jns3Logger.logError(name, "Received frame with zero source MAC address on port " + portNumber);
			return;
		}
		routerEthernetProtocol.putPortMapping(portNumber, srcMac);
		frameReceiver.receive(frame);
	}
}