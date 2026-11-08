package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.hardware.HomeRouter;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

public class RouterEthernetProtocol extends EthernetProtocol {
	HRFrameSender frameSender;
	HRMultiPortDevice multiPortDevice;
	PortMapper portMapper;
	private int numPorts;

	public RouterEthernetProtocol(HomeRouter device) {
		super(device);
		numPorts = device.getNumPorts();
		this.portMapper = new MultiPortMapper(numPorts);
		frameSender = new HRFrameSender(this);
		multiPortDevice = new HRMultiPortDevice(this, delegator);
	}

	@Override
	public void register(LowerAdapter<EthernetFrame> adapter) {
		adapter.setOutboundSender(frameSender);
		adapter.setInboundCallback(delegator);
		for(var h: adapter.getInboundHandlers()) {
			delegator.addHandler(h);
		}
	}

	public void send(EthernetFrame type) {
		frameSender.send(type);		
	}

	public int getPortForMacAddress(MACAddress destMac) {
		return portMapper.getPortMapping(destMac);
	}


	public void sendFrame(int i, EthernetFrame frame) {
		multiPortDevice.sendFrame(i, frame);
	}

	public void putPortMapping(int portNumber, MACAddress srcMac) {
		portMapper.putPortMapping(portNumber, srcMac);
	}

	public int getNumPorts() {
		return numPorts;
	}

	public PhysicalEndPoint getEndPoint(int i) {
		return multiPortDevice.getEndPoint(i);
	}

	
}
