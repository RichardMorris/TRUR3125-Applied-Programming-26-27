package org.singsurf.jns3.hardware;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;
import org.singsurf.jns3.network_layer.RoutingProtocol;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter extends ProtocolDevice implements PrefixDevice {
	public static final int NUM_PORTS = 4;
		
    Terminal terminal = new Terminal() {
		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};

	private IPPrefix publicPrefix;
		
	public HomeRouter(String name, MACAddress macAddress, IPAddress ipAddress, IPPrefix publicPrefix) {
        super(name,macAddress,ipAddress,NUM_PORTS, 1);
        this.publicPrefix = publicPrefix;
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
        
    public PhysicalEndPoint getEndPoint(int i) {
    	return getPhysicalProtocol().getEndPoint(i);
    }

	@Override
	public IPPrefix getPrefix() {
		return this.publicPrefix;
	}

	@Override
	public IPAddress getPublicIP() {
		var rp = getProtocol(RoutingProtocol.class);
		return rp != null ? rp.getPublicIP() : null;
	}

	public void registerDevice(int i, PrefixDevice theInternet) {
		var rp = getProtocol(RoutingProtocol.class);
		if(rp != null) {
			rp.registerDevice(i, theInternet);
		} else {
			Jns3Logger.logError(getName(), "RoutingProtocol not found, cannot register device");
		}		
	}
    
}
