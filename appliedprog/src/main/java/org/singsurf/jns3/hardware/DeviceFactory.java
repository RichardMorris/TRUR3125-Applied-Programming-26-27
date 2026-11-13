package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPrefix;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.RoutingProtocol;
import org.singsurf.jns3.network_layer.SubnetMask;
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

    private IPPrefix createPublicPrefix() {
        int[] bytes = new int[4];
        IPAddress ip = null;
        bytes[0] = 81;
        bytes[3] = 0;
        do {
            bytes[1] = (int) (Math.random() * 256);
            bytes[2] = (int) (Math.random() * 256);
            ip = new IPAddress(bytes);
        } while (ipAddresses.contains(ip));
        ipAddresses.add(ip);
        return new IPPrefix(ip, 24);
	}

    byte nextSequentialIP = 2;
    
    /**
	 * Creates a new sequential local IP address in the 192.168.1.x range, starting 
	 * from 2.
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
    
	public void resetSequentialLocalIPAddress() {
		nextSequentialIP = 2;		
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
        return createPC(name, ip, mac);
    }
        
    public PC createPC(String name, IPAddress ip, MACAddress mac) {
        // boot logic for the PC
        var pc = new PC(name, mac, ip);

        var physicalProtocol = new PhysicalProtocol(pc,1);
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
		pc.setSubnetMask(new SubnetMask(IPAddress.ZERO_ADDRESS));
        return pc;
    }

    public HomeRouter createHomeRouter(String name) {

    	HomeRouter router = new HomeRouter(name, 
				DeviceFactory.getInstance().createMACAddress(),
				new IPAddress("192.168.1.1"),
				DeviceFactory.getInstance().createPublicPrefix());

		var phys = new PhysicalProtocol(router, HomeRouter.NUM_PORTS);
		router.addProtocol(phys);
		
		var eth = new EthernetProtocol(router);
		router.addProtocol(eth);
		phys.register(eth);
		
		var arpProtocol= new ARPProtocol(router,true);
		router.addProtocol(arpProtocol);
		eth.register(arpProtocol);
		
		var routingProtocol = new RoutingProtocol(router);
		router.addProtocol(routingProtocol);

		var ipProtocol = new IPProtocol(router,true);
		router.addProtocol(ipProtocol);
		eth.register(ipProtocol);

		var imcp = new ICMPProtocol(router);
		ipProtocol.register(imcp);
		router.addProtocol(imcp);
		
		var udp = new UDPProtocol(router);
		ipProtocol.register(udp);
		router.addProtocol(udp);
		
		var dhcp = new DHCPProtocol(router,true);
		udp.register(dhcp);
		router.addProtocol(dhcp);
					
		router.setMulticastAddress(new IPAddress("192.168.1.255"));
		router.setSubnetMask(new SubnetMask("255.255.255.0"));

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
