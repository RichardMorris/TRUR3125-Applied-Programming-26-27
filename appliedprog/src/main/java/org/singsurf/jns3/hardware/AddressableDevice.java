package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.MACAddressable;
import org.singsurf.jns3.network_layer.IPAddressable;

public interface AddressableDevice extends NamedDevice, MACAddressable, IPAddressable{

}
