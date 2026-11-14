package org.singsurf.jns3.hardware;

import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;

public interface PrefixDevice extends AddressableDevice {
	IPPrefix getPrefix();
	IPAddress getPublicIP();
	void registerDevice(int i, PrefixDevice theInternet);
}
