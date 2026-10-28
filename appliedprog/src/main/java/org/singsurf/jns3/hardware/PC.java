package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.FrameDelegator;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.network_layer.EthernetPacketSender;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.PacketReceiver;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPPacketReceiver;
import org.singsurf.jns3.network_layer.icmp.PingController;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPController;

/**
 * The main class representing a PC.
 * Acts as a Facade to the various layers in the system.
 */
public class PC implements AddressableDevice, Terminal {
    String name;
    MACAddress macAddress;
    IPAddress ipAddress;
	IPAddress multiCastIP;

    ARPController arpController;
	FrameSender ethernetEndPoint;
	FrameDelegator frameReceiver;
	PacketReceiver packetReceiver;
    PacketSender packetSender;
	ICMPPacketReceiver icmpPacketReceiver;
	PingController pingController;
    UDPController udpController;

    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.frameReceiver = new FrameDelegator(name);
        this.ethernetEndPoint = new NICCard(name, this.frameReceiver);
        this.arpController = new ARPController(this,ethernetEndPoint);
        this.packetSender = new EthernetPacketSender(this, arpController, ethernetEndPoint);
    }

    public void addFrameHandler(FrameHandler frameHandler) {
    	frameReceiver.addHandler(frameHandler);
    }

    public void sendGARPRequest() {
        arpController.sendGARPRequest();
    }

    public void sendArpRequest(IPAddress targetIP) {
        arpController.sendArpRequest(targetIP);
    }

    public MACAddress lookup(IPAddress ipAddress) {
        return arpController.lookup(ipAddress);
    }

    @Override
    public String toString() {
        return "PC\t" + getName() + "\t" + getMACAddress() + "\t" + getIPAddress();
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public MACAddress getMACAddress() {
        return macAddress;
    }

    @Override
    public IPAddress getIPAddress() {
        return ipAddress;
    }

    public PhysicalEndPoint getEndPoint() {
        return (PhysicalEndPoint) ethernetEndPoint;
    }
    
    public FrameDelegator getFrameDelegator() {
		return frameReceiver;
	}
    
    public void setEndPoint(FrameSender endPoint) {
		this.ethernetEndPoint = endPoint;
	}

    public ARPController getArpController() {
        return arpController;
    }

//	public void clearFrameHandlers() {
//		frameReceiver.clear();		
//	}
	
	public FrameReceiver getFrameReceiver() {
		return frameReceiver;
	}
	
	public FrameSender getFrameSender() {
		return ethernetEndPoint;
	}
	
	public void setPacketReceiver(PacketReceiver packetReceiver) {
		this.packetReceiver = packetReceiver;
	}
	
	public PacketReceiver getPacketReceiver() {
		return packetReceiver;
	}
	
	public void setPacketSender(PacketSender packetSender) {
		this.packetSender = packetSender;
	}
	
	public PacketSender getPacketSender() {
		return packetSender;
	}
	
	public void display(String string) {
		System.out.println(string);
	}

	public void bind(DatagramSocket socket, IPAddress multicastAddress, short portNumber) {
		udpController.bind(multicastAddress, portNumber, socket);
		
	}
	

	public IPAddress getMulticastAddress() {
		return multiCastIP;
	}

	public void setMulticastAddress(IPAddress multiCastIP) {
		this.multiCastIP = multiCastIP;
		this.arpController.put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setICMPHandler(ICMPPacketReceiver icmpPacketReceiver) {
		this.icmpPacketReceiver = icmpPacketReceiver;
	}
	
	public ICMPPacketReceiver getICMPHandler() {
		return icmpPacketReceiver;
	}

	public void setUDPController(UDPController udpController2) {
		this.udpController = udpController2;
	}

	public UDPController getUDPController() {
		return udpController;
	}

	public void ping(IPAddress ipAddress2, int i) {
		pingController.sendPingRequest(ipAddress2, i);
	}

	public void setPingController(PingController newInstance) {
		this.pingController = newInstance;		
	}
	
	public PingController getPingController() {
		return pingController;
	}

	public void traceroute(IPAddress ipAddress2) {
		pingController.sendTraceroute(ipAddress2);
	}

	public void setIPAddress(IPAddress ip) {
		this.ipAddress = ip;
	}
}
