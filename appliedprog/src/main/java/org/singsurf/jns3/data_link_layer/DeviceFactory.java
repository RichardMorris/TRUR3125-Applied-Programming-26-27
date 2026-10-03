package org.singsurf.jns3.data_link_layer;

import java.util.List;
import java.util.ArrayList;

public class DeviceFactory {
    List<MACAddress> macAddresses = new ArrayList<>();

    public MACAddress createMACAddress() {
        int[] bytes = new int[6];
        MACAddress mac = null;
        do {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (int) (Math.random() * 256);
            }
            mac = new MACAddress(bytes);
        } while (macAddresses.contains(mac));
        macAddresses.add(mac);
        return mac;
    }

    public PC createPC(String name) {
        MACAddress mac = createMACAddress();
        return new PC(name, mac);
    }

}
