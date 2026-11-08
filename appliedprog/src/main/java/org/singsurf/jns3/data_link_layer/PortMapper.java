package org.singsurf.jns3.data_link_layer;

public interface PortMapper {

	public int getPortMapping(MACAddress destMac);
	
	public void putPortMapping(int portNumber, MACAddress srcMac);

}
