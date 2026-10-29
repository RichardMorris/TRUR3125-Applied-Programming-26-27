package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPMessageLogger;
import org.singsurf.jns3.network_layer.icmp.ICMPPacketReceiver;
import org.singsurf.jns3.network_layer.icmp.PingController;
import org.singsurf.jns3.network_layer.icmp.PingRequestHandler;
import org.singsurf.jns3.transport_layer.udp.UDPController;

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
        pc.setEthernetProtocol(ethernetProtocol);
        
        ARPProtocol arpProtocol = new ARPProtocol(pc);
        pc.setArpProtocol(arpProtocol);
        ethernetProtocol.register(arpProtocol);

		// Set up ICMP handler and add it to the packet receiver
        pc.setICMPHandler(new ICMPPacketReceiver(pc.getName()));

        IPProtocol ipProtocol = new IPProtocol(pc, arpProtocol);
        pc.setIPProtocol(ipProtocol);
        
        ethernetProtocol.register(ipProtocol);

        
		// Add frame handlers to the PC
        ethernetProtocol.finalise();

		pc.setPingController(PingController.newInstance(pc));
		pc.getICMPHandler().addHandler(pc.getPingController());
		pc.getICMPHandler().addHandler(new PingRequestHandler(pc, ipProtocol.getPacketSender()));
		pc.getICMPHandler().addHandler(new ICMPMessageLogger("ICMPLogger1"));
		
		// Set up UDP controller and add it to the packet receiver
		pc.setUDPController(new UDPController(pc, ipProtocol.getPacketSender()));

        ipProtocol.addHandler(pc.getICMPHandler());
        ipProtocol.addHandler(pc.getUDPController());
        ipProtocol.finalise();

		pc.setMulticastAddress(new IPAddress("192.168.1.255"));

        return pc;
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
