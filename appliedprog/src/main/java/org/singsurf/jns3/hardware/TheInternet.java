package org.singsurf.jns3.hardware;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;
import org.singsurf.jns3.network_layer.RoutingProtocol;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

public class TheInternet extends ProtocolDevice implements PrefixDevice {
	IPPrefix prefix = new IPPrefix("0.0.0.0/0");
	IPAddress publicIP;	

	public TheInternet(String name, MACAddress macAddress, IPAddress ipAddress, int numPorts) {
		super(name, macAddress, ipAddress, 0, numPorts);
		publicIP = ipAddress;
	}

	@Override
	public IPPrefix getPrefix() {
		return prefix;
	}

	@Override
	public Terminal getTerminal() {
		return null;
	}

	public void registerDevice(int i, PrefixDevice dev) {
		var rp = getProtocol(RoutingProtocol.class);
		if(rp != null) {
			rp.registerDevice(i, dev);
		} else {
			Jns3Logger.logError(getName(), "RoutingProtocol not found, cannot register device");
		}		
	}

	@Override
	public IPAddress getPublicIP() {
		return publicIP;
	}

    public PhysicalEndPoint getEndPoint(int i) {
    	return getPhysicalProtocol().getEndPoint(i);
    }
    
    @Override
    public String toString() {
        return "TheInternet\t" + getName() + "\t" + getMACAddress() 
        	+ "\t" + getIPAddress() + "\t" + getSubnetMask()
        	+ "\t" + getPublicIP() + "\t" + getPrefix();
    }

}
