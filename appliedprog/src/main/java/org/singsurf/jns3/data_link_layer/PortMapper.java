package org.singsurf.jns3.data_link_layer;

import java.util.List;

/**
 * Interface for mapping ports to MAC addresses in the Ethernet protocol.
 */
public interface PortMapper {

	/**
	 * Associates a source MAC address with a specific port number.
	 * 
	 * @param portNumber The port number to associate with the source MAC address.
	 * @param srcMac The source MAC address to be associated with the port number.
	 */
	public void putPortMapping(int portNumber, MACAddress srcMac);

	/**
	 * Returns a list of port numbers for the given destination MAC address.
	 * If the destination MAC address is a broadcast address, it returns all ports except the one
	 * associated with the source MAC address.
	 * 
	 * @param destMac The destination MAC address.
	 * @param srcMac used to determine which port to exclude in case of a broadcast address.
	 * @return A list of port numbers for the given destination and source MAC addresses.
	 */
	public List<Integer> getPortsForMacAddress(MACAddress destMac, MACAddress srcMac);
}
