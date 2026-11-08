package org.singsurf.jns3.hardware;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.RouterEthernetProtocol;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.physical_layer.PhysicalProtocol;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter extends ProtocolDevice {
	public static final int NUM_PORTS = 4;
	
	Map<Integer,Set<MACAddress>> portMapping = new HashMap<>();
	HRMultiPortDevice multiPortDevice;
	ProtocolDelegator<EthernetFrame> frameReceiver;
	
    Terminal terminal = new Terminal() {
		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};

	RouterEthernetProtocol routerEthernetProtocol;

		
		
	class HRMultiPortDevice extends MultiPortDevice {

		public HRMultiPortDevice() {
			super(HomeRouter.this.getName(), NUM_PORTS);
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
			var list = portMapping.get(portNumber);
			list.add(srcMac);
			frameReceiver.receive(frame);
		}
	}
	
	public HomeRouter(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name,macAddress,ipAddress);
        this.multiPortDevice = new HRMultiPortDevice();
        for(int i = 0; i < NUM_PORTS; i++) {
			portMapping.put(i, new HashSet<>());
		}
    }
    		
	public Terminal getTerminal() {
		return terminal;
	}

	// General information about the PC
	
    @Override
    public String toString() {
        return "HomeRouter\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
    }

    

    // Setters and Getters for various components
        
    public PhysicalEndPoint getEndPoint(int i) {
    	return multiPortDevice.getEndPoint(i);
    }

	public void setFrameReceiver(ProtocolDelegator<EthernetFrame> delegator) {
		this.frameReceiver = delegator;
	}

	@Override
	public PhysicalProtocol getPhysicalProtocol() {
		return null;
	}

	public void sendFrame(int i, EthernetFrame frame) {
		multiPortDevice.sendFrame(i, frame);
	}

	public int getPortForMacAddress(MACAddress destMac) {
		for(int i = 0; i < NUM_PORTS; i++) {
			var list = portMapping.get(i);
			if(list.contains(destMac)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void addProtocol(Protocol prot) {
		super.addProtocol(prot);
		if(prot instanceof RouterEthernetProtocol) {
			this.routerEthernetProtocol = (RouterEthernetProtocol) prot;
		}
	}

	public void send(EthernetFrame type) {
		routerEthernetProtocol.send(type);
		
	}
    
}
