package org.singsurf.jns3.data_link_layer;

import java.util.List;

public interface PortMapper {

	public void putPortMapping(int portNumber, MACAddress srcMac);

	public List<Integer> getPortsForMacAddress(MACAddress destMac, MACAddress srcMac);
}
