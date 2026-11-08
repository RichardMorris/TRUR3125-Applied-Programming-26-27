package org.singsurf.jns3.hardware;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.RouterEthernetProtocol;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.physical_layer.PhysicalProtocol;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter extends ProtocolDevice {
	public static final int NUM_PORTS = 4;
		
    Terminal terminal = new Terminal() {
		@Override
		public void display(String string) {
			Jns3Logger.logError(getName(), string);
		}};

	private RouterEthernetProtocol routerEthernetProtocol;

		
	public HomeRouter(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name,macAddress,ipAddress);
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
    	return routerEthernetProtocol.getEndPoint(i);
    }

	@Override
	public PhysicalProtocol getPhysicalProtocol() {
		return null;
	}

	public void sendFrame(int i, EthernetFrame frame) {
		routerEthernetProtocol.sendFrame(i, frame);
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

	public int getNumPorts() {
		return NUM_PORTS;
	}
    
}
