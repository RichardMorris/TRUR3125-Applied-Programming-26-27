package org.singsurf.jns3.hardware;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.generic_protocol.UpperPhysicalPort;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter0 extends ProtocolDevice implements AddressableDevice {
	NICCard nic;

	Terminal terminal = new Terminal() {

		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};


	public HomeRouter0(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name,macAddress,ipAddress);
        this.nic = new NICCard(name);
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
        
    public PhysicalEndPoint getEndPoint() {
        return nic;
    }

	@Override
	public UpperPhysicalPort getPhysicalProtocol() {
		return nic;
	}
    
}
