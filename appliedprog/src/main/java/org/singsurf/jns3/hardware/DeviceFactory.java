package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

/**
 * A factory for creating network devices such as PCs, switches, and hubs.
 * Also manages the creation of unique MAC and IP addresses for devices.
 */
public class DeviceFactory {
	static DeviceFactory instance = new DeviceFactory();
    List<MACAddress> macAddresses = new ArrayList<>();
    List<IPAddress> ipAddresses = new ArrayList<>();

    public DeviceFactory() {
        macAddresses.add(MACAddress.BROADCAST_MAC);
        ipAddresses.add(IPProtocol.LIMITED_MULTICAST_ADDRESS);
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

    byte nextSequentialIP = 1;
    public IPAddress createSequentialLocalIPAddress() {
        int[] bytes = new int[4];
        bytes[0] = 192;
        bytes[1] = 168;
        bytes[2] = 1;
        bytes[3] = nextSequentialIP;
        var ip = new IPAddress(bytes);
        ipAddresses.add(ip);
        ++nextSequentialIP;
        return ip;
    }

    public PC createPC(String name) {
        MACAddress mac = createMACAddress();
        IPAddress ip = createLocalIPAddress();
        var pc = new PC(name, mac, ip);
        
        // boot logic for the PC
		
        EthernetProtocol ethernetProtocol = new EthernetProtocol(pc);
        pc.addProtocol(ethernetProtocol);
        
        ARPProtocol arpProtocol = new ARPProtocol(pc);
        pc.addProtocol(arpProtocol);
        ethernetProtocol.register(arpProtocol);

        IPProtocol ipProtocol = new IPProtocol(pc, arpProtocol);
        pc.addProtocol(ipProtocol);
        ethernetProtocol.register(ipProtocol);

        ICMPProtocol icmpProtocol = new ICMPProtocol(pc,pc.getTerminal());
        ipProtocol.register(icmpProtocol);
        pc.addProtocol(icmpProtocol);
        		
        UDPProtocol udpProtocol = new UDPProtocol(pc);
        ipProtocol.register(udpProtocol);
        pc.addProtocol(udpProtocol);

        DHCPProtocol dhcpProtocol = new DHCPProtocol(pc, false, arpProtocol);
		udpProtocol.register(dhcpProtocol);
        pc.addProtocol(dhcpProtocol);

        ipProtocol.finalise();
        ethernetProtocol.finalise();

		pc.setMulticastAddress(new IPAddress("192.168.1.255"));

        return pc;
    }

    public HomeRouter createHomeRouter(String name) {
        MACAddress mac = createMACAddress();
        IPAddress ip = createSequentialLocalIPAddress();

        var router = new HomeRouter(name, mac, ip);
        
        // boot logic for the PC
		
        EthernetProtocol ethernetProtocol = new EthernetProtocol(router);
        router.addProtocol(ethernetProtocol);
        
        ARPProtocol arpProtocol = new ARPProtocol(router);
        router.addProtocol(arpProtocol);
        ethernetProtocol.register(arpProtocol);

        IPProtocol ipProtocol = new IPProtocol(router, arpProtocol);
        router.addProtocol(ipProtocol);
        ethernetProtocol.register(ipProtocol);

        ICMPProtocol icmpProtocol = new ICMPProtocol(router,new Terminal() {
			@Override
			public void display(String string) {
				Jns3Logger.logError(name, string);
			}});
        ipProtocol.register(icmpProtocol);
        router.addProtocol(icmpProtocol);
        		
        UDPProtocol udpProtocol = new UDPProtocol(router);
        ipProtocol.register(udpProtocol);
        router.addProtocol(udpProtocol);

        DHCPProtocol dhcpProtocol = new DHCPProtocol(router, true, arpProtocol);
		udpProtocol.register(dhcpProtocol);
        router.addProtocol(dhcpProtocol);

        ipProtocol.finalise();
        ethernetProtocol.finalise();

		router.setMulticastAddress(new IPAddress("192.168.1.255"));

        return router;
    }

    public Switch createSwitch(String name, int numPorts) {
        var sw = new Switch(name, numPorts);
        return sw;
    }

    public Hub createHub(String name, int numPorts) {
        var hub = new Hub(name, numPorts);
        return hub;
    }

	public static DeviceFactory getInstance() {
		return instance;
	}
}
