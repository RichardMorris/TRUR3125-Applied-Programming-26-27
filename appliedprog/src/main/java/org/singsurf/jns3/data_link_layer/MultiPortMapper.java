package org.singsurf.jns3.data_link_layer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiPortMapper implements PortMapper {

	Map<Integer,Set<MACAddress>> portMapping = new HashMap<>();
	private int numPorts;
	private int numInternalPorts;

	public MultiPortMapper(int numInternalPorts, int numExternalPorts) {
		this.numPorts = numInternalPorts + numExternalPorts;
		this.numInternalPorts = numInternalPorts;
	    for(int i = 0; i < numPorts; i++) {
	    	portMapping.put(i, new HashSet<>());
		}
	}

	private int getPortMapping(MACAddress destMac) {
		for(int i = 0; i < numPorts; i++) {
			var list = portMapping.get(i);
			if(list.contains(destMac)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void putPortMapping(int portNumber, MACAddress srcMac) {
		var list = portMapping.get(portNumber);
		list.add(srcMac);
	}

	@Override
	public List<Integer> getPortsForMacAddress(MACAddress destMac, MACAddress srcMac) {
		List<Integer> ports = new java.util.ArrayList<>();
		if(destMac.equals(MACAddress.BROADCAST_MAC)) {
			int index = getPortMapping(srcMac);
			for(int i = 0; i < numInternalPorts; i++) {
				if(i != index) {
					ports.add(i);
				}
			}
		} else {
			int index = getPortMapping(destMac);
			if(index != -1) {
				ports.add(index);
				return ports;
			}
		}
		return ports;
	}

}
