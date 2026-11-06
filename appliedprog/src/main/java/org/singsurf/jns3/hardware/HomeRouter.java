package org.singsurf.jns3.hardware;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter extends ProtocolDevice implements AddressableDevice {
	public static final int NUM_PORTS = 4;
	
	Map<Integer,Set<MACAddress>> portMapping = new HashMap<>();
	HRMultiPortDevice multiPortDevice;
	HRFrameSender frameSender = new HRFrameSender();
	EthernetProtocol ethernetProtocol;

    Terminal terminal = new Terminal() {
		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};

		
		
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
			ethernetProtocol.getFrameDelegator().receive(frame);
		}
	}
	
	@Override
	public void addProtocol(Protocol prot) {
		super.addProtocol(prot);
		if(prot instanceof EthernetProtocol rep) {
			ethernetProtocol =rep;
		}
	}



	class HRFrameSender implements FrameSender {
		HRFrameSender() { }
		
		@Override
		public void send(EthernetFrame frame) {
			var destMac = frame.getDestination();
			var srcMac = frame.getSource();
			if(destMac.equals(MACAddress.BROADCAST_MAC)) {
				for(int i = 0; i < NUM_PORTS; i++) {
					var list = portMapping.get(i);
					if(!list.contains(srcMac)) {
						multiPortDevice.sendFrame(i, frame);
					}
				}
				return;
			} else {
				for(int i = 0; i < NUM_PORTS; i++) {
					var list = portMapping.get(i);
					if(list.contains(destMac)) {
						multiPortDevice.sendFrame(i, frame);
						return;
					}
				}
				Jns3Logger.logError(getName(), "Destination MAC address " + destMac + " not found in any port mapping");
			}
		}	
	}

	class HRFrameReceiver implements FrameReceiver {
		HRFrameReceiver() { }
		
		@Override
		public void receive(EthernetFrame frame) {
			frameSender.send(frame);
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
    
}
