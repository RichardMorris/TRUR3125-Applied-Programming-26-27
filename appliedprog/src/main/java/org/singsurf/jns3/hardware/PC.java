package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * The main class representing a PC.
 * Acts as a Facade to the various layers in the system.
 */
public class PC extends ProtocolDevice implements AddressableDevice {
	PhysicalEndPoint endPoint;
	
    Terminal terminal = new Terminal() {

		@Override
		public void display(String string) {
			System.out.println(string);
		}};


	public PC(String name, MACAddress macAddress, IPAddress ipAddress, PhysicalEndPoint endPoint) {
        super(name,macAddress,ipAddress);
        this.endPoint = endPoint;
    }
    
	

    // Commands available to the user of the PC

	public void ping(IPAddress ipAddress2, int i) {
		getICMPProtocol().sendPingRequest(ipAddress2, i);
	}

	public void traceroute(IPAddress ipAddress2) {
		getICMPProtocol().sendTraceroute(ipAddress2);
	}
	
	public Terminal getTerminal() {
		return terminal;
	}

	// General information about the PC
	
    @Override
    public String toString() {
        return "PC\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
    }

    // Setters and Getters for various components
        
    public PhysicalEndPoint getEndPoint() {
        return endPoint;
    }
    
}
