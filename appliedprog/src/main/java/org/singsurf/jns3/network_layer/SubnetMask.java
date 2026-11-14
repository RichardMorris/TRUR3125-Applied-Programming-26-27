package org.singsurf.jns3.network_layer;

/**
 * Represents a subnet mask used in IP networking e.g {@code 255.255.255.0}.
 * to determine the network and host portions of an IP address.
 * <p>
 * The {@link #matches(IPAddress, IPAddress)} 
 * method can be used to check if two IP addresses belong to the same subnet.
 * The logic is based on performing a bitwise AND operation between the subnet mask 
 * and each of the IP addresses, and then comparing the results.
 */
public class SubnetMask extends IPAddress {

	public SubnetMask(String addr) {
		super(addr);
	}

	public SubnetMask(IPAddress maskIP) {
		super(maskIP.getBytes());
	}

	/**
	 * Checks if two IP addresses belong to the same subnet based on this subnet mask.
	 * 
	 * @param src The source IP address.
	 * @param dst The destination IP address.
	 * @return true if both IP addresses belong to the same subnet; false otherwise.
	 */
	public boolean matches(IPAddress src, IPAddress dst) {
		var srcBytes = src.getBytes();
		var dstBytes = dst.getBytes();
		var maskBytes = this.getBytes();
		for(int i=0; i<4; i++) {
			if((srcBytes[i] & maskBytes[i]) != (dstBytes[i] & maskBytes[i])) {
				return false;
			}
		}
		return true;
	}
}
