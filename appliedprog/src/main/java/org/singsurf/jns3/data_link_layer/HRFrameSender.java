package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.HomeRouter;

class HRFrameSender implements FrameSender {
	/**
	 * 
	 */
	private final RouterEthernetProtocol homeRouter;

	HRFrameSender(RouterEthernetProtocol routerEthernetProtocol) {
		this.homeRouter = routerEthernetProtocol; }
	
	@Override
	public void send(EthernetFrame frame) {
		var destMac = frame.getDestination();
		var srcMac = frame.getSource();
		if(destMac.equals(MACAddress.BROADCAST_MAC)) {
			int index = homeRouter.getPortForMacAddress(srcMac);
			for(int i = 0; i < HomeRouter.NUM_PORTS; i++) {
				if(i != index) {
					homeRouter.sendFrame(i, frame);
				}
			}
			return;
		} else {
			int index = homeRouter.getPortForMacAddress(destMac);
			if(index != -1) {
					homeRouter.sendFrame(index, frame);
					return;
			}
			Jns3Logger.logError(homeRouter.getName(), "Destination MAC address " + destMac + " not found in any port mapping");
		}
	}	
}