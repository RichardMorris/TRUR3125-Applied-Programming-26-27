package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.hardware.PrefixDevice;

public class RoutingProtocol implements Protocol {
	String name;
	
	NATTable natTable = new NATTable();
	IPPrefix publicPrefix;
	List<IPAddress> allocatedPublicIPs = new ArrayList<>();
	byte seqNumber = 0;

	private IPAddress publicIP;
	
	public RoutingProtocol(PrefixDevice device) {
		this.name = device.getName() + "NAT";
		this.publicPrefix = device.getPrefix();
		this.publicIP = allocatePublicIP(device.getIPAddress());
	}


	public IPAddress allocatePublicIP(IPAddress iaddr) {
		if(seqNumber == 255) {
			Jns3Logger.logError(name,"No more available addresses");
			return null;
		}
		var preIP = publicPrefix.getPrefix();
		var bytes = preIP.getBytes();
		bytes[3] = seqNumber++;
		var publicIP = new IPAddress(bytes);
		natTable.put(iaddr, publicIP);
		allocatedPublicIPs.add(publicIP);
		return publicIP;
	}


	public IPAddress getPublicIP() {
		return publicIP;
	}


	public IPAddress getPublicIP(IPAddress ipAddress) {
		return natTable.getPublicIP(ipAddress);
	}
	
	public IPAddress getPrivateIP(IPAddress publicIP) {
		return natTable.getPrivateIP(publicIP);
	}

}
