package org.singsurf.jns3.data_link_layer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiPortMapper implements PortMapper {

	Map<Integer,Set<MACAddress>> portMapping = new HashMap<>();
	private int numPorts;

	public MultiPortMapper(int numPorts) {
		this.numPorts = numPorts;
	    for(int i = 0; i < numPorts; i++) {
	    	portMapping.put(i, new HashSet<>());
		}
	}

	@Override
	public int getPortMapping(MACAddress destMac) {
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

}
