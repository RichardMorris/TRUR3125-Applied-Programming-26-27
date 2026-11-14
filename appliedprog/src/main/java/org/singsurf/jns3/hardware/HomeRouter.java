package org.singsurf.jns3.hardware;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system,
 * has four internal ports and one external port.
 */
public class HomeRouter extends RoutingDevice {
	public static final int NUM_PORTS = 4;
		
    Terminal terminal = new Terminal() {
		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};
		
	public HomeRouter(String name, MACAddress macAddress, IPAddress ipAddress, IPPrefix publicPrefix) {
        super(name,macAddress,ipAddress,publicPrefix, NUM_PORTS, 1);
     }
    		
	public Terminal getTerminal() {
		return terminal;
	}

	// General information about the PC
	
    @Override
    public String toString() {
        return "HomeRouter\t" + getName() + "\t" + getMACAddress() 
        	+ "\t" + getIPAddress() + "\t" + getSubnetMask()
        	+ "\t" + getPublicIP() + "\t" + getPrefix();
    }

    // Setters and Getters for various components
        
	@Override
	public IPAddress getPublicIP() {
		var rp = getRoutingProtocol();
		return rp != null ? rp.getPublicIP() : null;
	}

	/**
	 * Get the public IP address corresponding to a private IP address.
	 * @param ipAddress The private IP address.
	 * @return The public IP
	 */
	public IPAddress getPublicIP(IPAddress ipAddress) {
		var rp = getRoutingProtocol();
		return rp.getPublicIP(ipAddress);
	}
    
}
