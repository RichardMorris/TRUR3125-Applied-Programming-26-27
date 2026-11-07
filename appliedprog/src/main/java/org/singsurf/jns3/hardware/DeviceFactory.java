package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.physical_layer.PhysicalProtocol;
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
    
    /**
	 * Creates a new sequential local IP address in the 192.168.1.x range.
	 * First call should be reserved for the router, subsequent calls for PCs.
	 * @return A new sequential local IP address
	 */
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
    
    byte nextSequentialMAC = 0;
    
    public MACAddress createSequentialMACAddress() {
		int[] bytes = new int[6];
		bytes[0] = 0x11;
		bytes[1] = 0x22;
		bytes[2] = 0x33;
		bytes[3] = 0x44;
		bytes[4] = 0x55;
		bytes[5] = nextSequentialMAC;
		var mac = new MACAddress(bytes);
		macAddresses.add(mac);
		++nextSequentialMAC;
		return mac;
	}

    public PC createPC(String name) {
        MACAddress mac = createMACAddress();
        IPAddress ip = createLocalIPAddress();
        
        // boot logic for the PC
        var pc = new PC(name, mac, ip);

        var physicalProtocol = new PhysicalProtocol(pc);
        pc.addProtocol(physicalProtocol);

        EthernetProtocol ethernetProtocol = new EthernetProtocol(pc);
        pc.addProtocol(ethernetProtocol);
        physicalProtocol.register(ethernetProtocol);
        
        ARPProtocol arpProtocol = new ARPProtocol(pc,false);
        pc.addProtocol(arpProtocol);
        ethernetProtocol.register(arpProtocol);

        IPProtocol ipProtocol = new IPProtocol(pc,false);
        pc.addProtocol(ipProtocol);
        ethernetProtocol.register(ipProtocol);

        ICMPProtocol icmpProtocol = new ICMPProtocol(pc);
        ipProtocol.register(icmpProtocol);
        pc.addProtocol(icmpProtocol);
        		
        UDPProtocol udpProtocol = new UDPProtocol(pc);
        ipProtocol.register(udpProtocol);
        pc.addProtocol(udpProtocol);

        DHCPProtocol dhcpProtocol = new DHCPProtocol(pc, false);
		udpProtocol.register(dhcpProtocol);
        pc.addProtocol(dhcpProtocol);

        ipProtocol.finalise();
        ethernetProtocol.finalise();

		pc.setMulticastAddress(new IPAddress("192.168.1.255"));

        return pc;
    }

    public HomeRouter0 createHomeRouter(String name) {
        MACAddress mac = createMACAddress();
        IPAddress ip = createSequentialLocalIPAddress();

        var router = new HomeRouter0(name, mac, ip);
        
        // boot logic for the PC
        var physicalProtocol = new PhysicalProtocol(router);
        router.addProtocol(physicalProtocol);


        EthernetProtocol ethernetProtocol = new EthernetProtocol(router);
        router.addProtocol(ethernetProtocol);
        router.getPhysicalProtocol().register(ethernetProtocol);
        
        ARPProtocol arpProtocol = new ARPProtocol(router,true);
        router.addProtocol(arpProtocol);
        ethernetProtocol.register(arpProtocol);

        IPProtocol ipProtocol = new IPProtocol(router,false);
        router.addProtocol(ipProtocol);
        ethernetProtocol.register(ipProtocol);

        ICMPProtocol icmpProtocol = new ICMPProtocol(router);
        ipProtocol.register(icmpProtocol);
        router.addProtocol(icmpProtocol);
        		
        UDPProtocol udpProtocol = new UDPProtocol(router);
        ipProtocol.register(udpProtocol);
        router.addProtocol(udpProtocol);

        DHCPProtocol dhcpProtocol = new DHCPProtocol(router, true);
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
