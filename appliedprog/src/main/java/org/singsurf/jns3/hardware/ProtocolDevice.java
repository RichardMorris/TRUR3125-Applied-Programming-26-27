package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.SubnetMask;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.physical_layer.PhysicalProtocol;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * Abstract class representing a network device that 
 * has a protocol stack and can be addressed using MAC and IP addresses.
 */
public abstract class ProtocolDevice implements AddressableDevice {
	
	String name;
	MACAddress macAddress;
	IPAddress ipAddress;
	IPAddress multiCastIP;
	SubnetMask subnetMask;
	IPAddress defaultGateway;
	
	List<Protocol> protocols = new ArrayList<>();
	int numInternalPorts;
	int numExternalPorts;

	public ProtocolDevice(String name, 
			MACAddress macAddress, IPAddress ipAddress,
			int numPorts) {
		this.name = name;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.numInternalPorts = numPorts;
		this.numExternalPorts = 0;
	}

	public ProtocolDevice(String name, 
			MACAddress macAddress, IPAddress ipAddress,
			int numInternalPorts, int numExternalPorts) {
		this.name = name;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.numInternalPorts = numInternalPorts;
		this.numExternalPorts = numExternalPorts;
	}

	public void addProtocol(Protocol prot) {
		protocols.add(prot);
	}

	public <T extends Protocol> T getProtocol(Class<T> cl) {
		for(Protocol p: protocols) {
			if(cl.isInstance(p)) {
				return cl.cast(p);
			}
		}
		Jns3Logger.logError(name, "Protocol " + cl.getSimpleName() + " not found in device " + name);
		return null;
	}

	public PhysicalProtocol getPhysicalProtocol() {
		return getProtocol(PhysicalProtocol.class);
	}

	public EthernetProtocol getEthernetProtocol() {
		return getProtocol(EthernetProtocol.class);
	}

	public ARPProtocol getArpProtocol() {
		return getProtocol(ARPProtocol.class);
	}

	public IPProtocol getIPProtocol() {
		return getProtocol(IPProtocol.class);
	}

	public ICMPProtocol getICMPProtocol() {
		return getProtocol(ICMPProtocol.class);
	}

	public UDPProtocol getUDPProtocol() {
		return getProtocol(UDPProtocol.class);
	}

	public DHCPProtocol getDHCPProtocol() {
		return getProtocol(DHCPProtocol.class);
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

	public void setMulticastAddress(IPAddress ip) {
		multiCastIP = ip;
		var ap = getArpProtocol();
		if(ap!=null)
			ap.put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		ipAddress = ip;
	}

	public abstract Terminal getTerminal();

	public SubnetMask getSubnetMask() {
		return subnetMask;
	}

	public void setSubnetMask(SubnetMask subnetMask) {
		this.subnetMask = subnetMask;		
	}

	public int getNumInternalPorts() {
		return numInternalPorts;
	}
	
	public int getNumExternalPorts() {
		return numExternalPorts;
	}

	public IPAddress getDefaultGateway() {
		return defaultGateway;
	}

	public void setDefaultGateway(IPAddress defaultGateway) {
		this.defaultGateway = defaultGateway;
	}

	public int getNumPorts() {
		return numInternalPorts + numExternalPorts;
	}
	

}