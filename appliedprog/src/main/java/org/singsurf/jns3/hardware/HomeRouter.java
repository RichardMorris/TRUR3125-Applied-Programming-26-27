package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * Represents a home router.
 * Acts as a Facade to the various layers in the system.
 */
public class HomeRouter implements AddressableDevice {
    String name;
    MACAddress macAddress;
    IPAddress ipAddress;
	IPAddress multiCastIP;
	    
	EthernetProtocol ethernetProtocol;
    ARPProtocol arpProtocol;
	IPProtocol ipProtocol;
	ICMPProtocol icmpProtocol;
    UDPProtocol udpProtocol;
	private DHCPProtocol dhcpProtocol;

    public void setIcmpProtocol(ICMPProtocol icmpProtocol) {
		this.icmpProtocol = icmpProtocol;
	}

	public HomeRouter(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }
    
	public void setEthernetProtocol(EthernetProtocol ethernetProtocol) {
		this.ethernetProtocol = ethernetProtocol;
	}
	
	public EthernetProtocol getEthernetProtocol() {
		return ethernetProtocol;
	}

	public void setIPProtocol(IPProtocol ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	public IPProtocol getIPProtocol() {
		return ipProtocol;
	}
	
	public void setArpProtocol(ARPProtocol arpProtocol) {
		this.arpProtocol = arpProtocol;
	}

	public void setUDPProtocol(UDPProtocol udpProtocol) {
		this.udpProtocol = udpProtocol;			
	}

	public ARPProtocol getArpProtocol() {
		return arpProtocol;
	}
	
	public UDPProtocol getUDPProtocol() {
		return udpProtocol;
	}

	
	public void setDHCPProtocol(DHCPProtocol dhcpProtocol) {
		this.dhcpProtocol = dhcpProtocol;
	}
	
	public DHCPProtocol getDHCPProtocol() {
		return dhcpProtocol;
	}

    // Commands available to the user of the PC

	public void ping(IPAddress ipAddress2, int i) {
		icmpProtocol.sendPingRequest(ipAddress2, i);
	}

	public void traceroute(IPAddress ipAddress2) {
		icmpProtocol.sendTraceroute(ipAddress2);
	}

	// System commands available application code
	
	public void bind(DatagramSocket socket, IPAddress multicastAddress, short portNumber) {
		udpProtocol.bind(multicastAddress, portNumber, socket);		
	}

	// General information about the PC
	
    @Override
    public String toString() {
        return "HomeRouter\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
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
		arpProtocol.put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		this.ipAddress = ip;
	}

    // Setters and Getters for various components
        
    public PhysicalEndPoint getEndPoint() {
        return getEthernetProtocol().getEndPoint();
    }
    
}
