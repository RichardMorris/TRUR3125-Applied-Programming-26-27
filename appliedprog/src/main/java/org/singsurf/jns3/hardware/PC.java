package org.singsurf.jns3.hardware;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * The main class representing a PC.
 * Acts as a Facade to the various layers in the system.
 */
public class PC implements AddressableDevice {
    String name;
    MACAddress macAddress;
    IPAddress ipAddress;
	IPAddress multiCastIP;
	        
    List<Protocol> protocols = new ArrayList<>();
    Terminal terminal = new Terminal() {

		@Override
		public void display(String string) {
			System.out.println(string);
		}};


	public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }
    
	public EthernetProtocol getEthernetProtocol() {
    	return getProtocol(EthernetProtocol.class);
	}

	public IPProtocol getIPProtocol() {
    	return getProtocol(IPProtocol.class);
	}
	
	public ARPProtocol getArpProtocol() {
    	return getProtocol(ARPProtocol.class);
	}

	@SuppressWarnings("unchecked")
	public <T extends Protocol> T getProtocol(Class<T> cl) {
		for(Protocol p: protocols) {
			if(cl.isInstance(p)) {
				return (T) p;
			}
		}
		return null;
	}

	public UDPProtocol getUDPProtocol() {
		return getProtocol(UDPProtocol.class);
	}

    // Commands available to the user of the PC

	public void ping(IPAddress ipAddress2, int i) {
		getICMPProtocol().sendPingRequest(ipAddress2, i);
	}

	public ICMPProtocol getICMPProtocol() {
		return getProtocol(ICMPProtocol.class);
	}

	public void traceroute(IPAddress ipAddress2) {
		getICMPProtocol().sendTraceroute(ipAddress2);
	}
	
	public DHCPProtocol getDHCPProtocol() {
		return getProtocol(DHCPProtocol.class);
	}


	// System commands available application code
	
	public void bind(DatagramSocket socket, IPAddress multicastAddress, short portNumber) {
		getUDPProtocol().bind(multicastAddress, portNumber, socket);		
	}

	public Terminal getTerminal() {
		return terminal;
	}


	// General information about the PC
	
    @Override
    public String toString() {
        return "PC\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public MACAddress getMACAddress() {
        return macAddress;
    }

    @Override
    public IPAddress getIPAddress() {
        return ipAddress;
    }
    
	public IPAddress getMulticastAddress() {
		return multiCastIP;
	}

	// system calls
	
	public void setMulticastAddress(IPAddress multiCastIP) {
		this.multiCastIP = multiCastIP;
		getArpProtocol().put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		this.ipAddress = ip;
	}

    // Setters and Getters for various components
        
    public PhysicalEndPoint getEndPoint() {
        return getEthernetProtocol().getEndPoint();
    }

	public void addProtocol(Protocol prot) {
		protocols.add(prot);
	}
    
}
