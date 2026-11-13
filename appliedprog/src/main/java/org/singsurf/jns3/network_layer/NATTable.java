package org.singsurf.jns3.network_layer;

import java.util.HashMap;
import java.util.Map;

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
