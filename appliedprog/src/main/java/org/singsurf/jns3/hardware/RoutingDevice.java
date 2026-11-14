package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;
import org.singsurf.jns3.network_layer.RoutingProtocol;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * Devices that can route traffic based on an {@link IPPrefix}.
 * They have a public IP address, a private IP address
 * and an IP prefix.
 * <p>
 * other devices can be manually registered via {@link #registerDevice(int, PrefixDevice)} 
 * establishing a mapping prefix's and hardware port number.
 * The {@link RoutingProtocol} 
 * is used to translate local to public IP's.
 */
public abstract class RoutingDevice extends ProtocolDevice implements PrefixDevice {

	IPPrefix prefix;
	protected IPAddress publicIP;

	public RoutingDevice(String name, MACAddress macAddress, 
			IPAddress localIP, IPPrefix prefix, int numInternalPorts,
			int numExternalPorts) {
		super(name, macAddress, localIP, numInternalPorts, numExternalPorts);
		this.prefix = prefix;
	}

	public void registerDevice(int i, PrefixDevice dev) {
		var rp = getRoutingProtocol();
		rp.registerDevice(i, dev);
	}

	@Override
	public IPPrefix getPrefix() {
		return prefix;
	}

	@Override
	public IPAddress getPublicIP() {
		return publicIP;
	}

	public PhysicalEndPoint getEndPoint(int i) {
    	return getPhysicalProtocol().getEndPoint(i);
    }

}