package org.singsurf.jns3.hardware;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * The main class representing a PC.
 * Acts as a Facade to the various layers in the system.
 */
public class PC extends ProtocolDevice {

	
	public static class LoggingTerminal implements Terminal {
		public boolean quite = false;
    	public List<String> lines = new ArrayList<>();
		@Override
		public void display(String string) {
			if(!quite)
				System.out.println(string);
			lines.add(string);
		}
	}
    public LoggingTerminal terminal = new LoggingTerminal();

	public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name,macAddress,ipAddress, 1);
	}
    
	

    // Commands available to the user of the PC

	public void ping(IPAddress ipAddress2, int i) {
		getICMPProtocol().sendPingRequest(ipAddress2, i);
	}

	public void traceroute(IPAddress ipAddress2) {
		getICMPProtocol().sendTraceroute(ipAddress2);
	}
	
	public void ipconfig() {
		getIPProtocol().displayIPConfig();
	}
	
	public LoggingTerminal getTerminal() {
		return terminal;
	}

	// General information about the PC
	
    @Override
    public String toString() {
        return "PC\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
    }

    // Setters and Getters for various components
        
    public PhysicalEndPoint getEndPoint() {
        return 	getPhysicalProtocol().getEndPoint(0);
    }
    
}
