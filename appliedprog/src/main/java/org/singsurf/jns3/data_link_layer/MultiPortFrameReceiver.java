package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;

/**
 * Receives frames from multiple ports.
 * Updates the port mapping based on the source MAC address of received frames.
 */
class MultiPortFrameReceiver {
	FrameReceiver frameReceiver;
	EthernetProtocol ethernetProtocol;

	public MultiPortFrameReceiver(EthernetProtocol rep, FrameReceiver delegator) {
		this.ethernetProtocol = rep;
		this.frameReceiver = delegator;
	}

	public void receiveFrame(int portNumber, EthernetFrame frame) {
		var srcMac = frame.getSource();
		if(srcMac.equals(MACAddress.BROADCAST_MAC)) {
			Jns3Logger.logError(ethernetProtocol.getName(), "Received frame with broadcast source MAC address on port " + portNumber);
			return;
		}
		if(srcMac.equals(MACAddress.ZERO_MAC)) {
			Jns3Logger.logError(ethernetProtocol.getName(), "Received frame with zero source MAC address on port " + portNumber);
			return;
		}
		ethernetProtocol.putPortMapping(portNumber, srcMac);
		frameReceiver.receive(frame);
	}
}