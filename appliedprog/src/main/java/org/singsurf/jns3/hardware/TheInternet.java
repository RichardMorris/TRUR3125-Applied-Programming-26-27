package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;

/**
 * Represents the Internet as a routing device.
 * It has a public IP address and a prefix of 0.0.0.0/0 that matches all IP addresses.
 * It has a number of ports that can be used to connect to other devices.
 */ 
public class TheInternet extends RoutingDevice {
	public TheInternet(String name, MACAddress macAddress, IPAddress ipAddress, int numPorts) {
		super(name, macAddress, ipAddress, new IPPrefix("0.0.0.0/0"), 0, numPorts);
		publicIP = ipAddress;
	}

	@Override
	public Terminal getTerminal() {
		return null;
	}
    
    @Override
    public String toString() {
        return "TheInternet\t" + getName() + "\t" + getMACAddress() 
        	+ "\t" + getIPAddress() + "\t" + getSubnetMask()
        	+ "\t" + getPublicIP() + "\t" + getPrefix();
    }

}
