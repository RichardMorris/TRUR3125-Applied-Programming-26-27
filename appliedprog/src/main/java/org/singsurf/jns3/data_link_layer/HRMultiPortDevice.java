package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.hardware.MultiPortDevice;

class HRMultiPortDevice extends MultiPortDevice {
	ProtocolReceiver<EthernetFrame> frameReceiver;
	RouterEthernetProtocol routerEthernetProtocol;

	public HRMultiPortDevice(RouterEthernetProtocol rep, ProtocolReceiver<EthernetFrame> delegator) {
		super(rep.getName(), rep.getNumPorts());
		this.routerEthernetProtocol = rep;
		this.frameReceiver = delegator;
	}

	@Override
	public void receiveFrame(int portNumber, EthernetFrame frame) {
		var srcMac = frame.getSource();
		if(srcMac.equals(MACAddress.BROADCAST_MAC)) {
			Jns3Logger.logError(getName(), "Received frame with broadcast source MAC address on port " + portNumber);
			return;
		}
		if(srcMac.equals(MACAddress.ZERO_MAC)) {
			Jns3Logger.logError(getName(), "Received frame with zero source MAC address on port " + portNumber);
			return;
		}
		routerEthernetProtocol.putPortMapping(portNumber, srcMac);
		frameReceiver.receive(frame);
	}
}