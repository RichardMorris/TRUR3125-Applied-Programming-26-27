package org.singsurf.jns3.hardware;

import java.util.List;

import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.DefaultFrameHandler;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.IPAddress;
import org.singsurf.jns3.data_link_layer.MACAddress;

import java.util.ArrayList;

public class DeviceFactory {
    List<MACAddress> macAddresses = new ArrayList<>();
    List<IPAddress> ipAddresses = new ArrayList<>();
    FrameFactory frameFactory = new FrameFactory();

    public DeviceFactory() {
        macAddresses.add(ARPController.ARP_BROADCAST_MAC);
    }

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

    /**
     * Creates a new local IP address in the 192.168.x.x range.
     * @return A new local IP address
     */
    public IPAddress createLocalIPAddress() {
        int[] bytes = new int[4];
        IPAddress ip = null;
        bytes[0] = 192;
        bytes[1] = 168;
        do {
            bytes[2] = (int) (Math.random() * 256);
            bytes[3] = (int) (Math.random() * 256);
            ip = new IPAddress(bytes);
        } while (ipAddresses.contains(ip));
        ipAddresses.add(ip);
        return ip;
    }

    public PC createPC(String name) {
        MACAddress mac = createMACAddress();
        IPAddress ip = createLocalIPAddress();
        var pc = new PC(name, mac, ip);
        pc.setFrameFactory(frameFactory);
        pc.setArpController(new ARPController(pc));
        pc.addFrameHandler(new DefaultFrameHandler(pc));
        return pc;
    }
}
