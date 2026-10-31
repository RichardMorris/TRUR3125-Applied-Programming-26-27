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
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

public class ProtocolDevice implements AddressableDevice {
	
	String name;
	MACAddress macAddress;
	IPAddress ipAddress;
	IPAddress multiCastIP;
	
	List<Protocol> protocols = new ArrayList<>();

	public ProtocolDevice() {
		super();
	}

	public ProtocolDevice(String name, MACAddress macAddress, IPAddress ipAddress) {
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

	public ICMPProtocol getICMPProtocol() {
		return getProtocol(ICMPProtocol.class);
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
		getArpProtocol().put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		ipAddress = ip;
	}

	public void addProtocol(Protocol prot) {
		protocols.add(prot);
	}

}