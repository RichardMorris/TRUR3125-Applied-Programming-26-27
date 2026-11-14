package org.singsurf.jns3.hardware;

import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;

/**
 * A device that has an IP prefix and a public IP address.
 * It can register other devices and translate between private and public IP addresses.
 */
public interface PrefixDevice extends AddressableDevice {
	IPPrefix getPrefix();
	IPAddress getPublicIP();
	void registerDevice(int i, PrefixDevice theInternet);
}
