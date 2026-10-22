package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.MACAddressable;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPAddressable;

public interface AddressableDevice extends NamedDevice, MACAddressable, IPAddressable {
	
	IPAddress getMulticastAddress();

	public static AddressableDevice createAddressableDevice(String name, MACAddress mac, IPAddress ip) {
		return new AddressableDevice() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public MACAddress getMACAddress() {
				return mac;
			}

			@Override
			public IPAddress getIPAddress() {
				return ip;
			}
			
			@Override
			public IPAddress getMulticastAddress() {
				return null;
			}
		};	
	}
}