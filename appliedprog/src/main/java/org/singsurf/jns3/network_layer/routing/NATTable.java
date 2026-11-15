package org.singsurf.jns3.network_layer.routing;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.network_layer.IPAddress;

/**
 * A simple NAT (Network Address Translation) table that maps private IP addresses 
 * to public IP addresses and vice versa.
 */
public class NATTable {
	Map<IPAddress, IPAddress> natMap = new HashMap<>();
	Map<IPAddress, IPAddress> reverseNatMap = new HashMap<>();
	
	public NATTable() {
	}

	public void put(IPAddress privateIP, IPAddress publicIP) {
		natMap.put(privateIP, publicIP);
		reverseNatMap.put(publicIP, privateIP);
	}
	
	public IPAddress getPublicIP(IPAddress privateIP) {
		return natMap.get(privateIP);
	}
	
	public IPAddress getPrivateIP(IPAddress publicIP) {
		return reverseNatMap.get(publicIP);
	}
}
