package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.FrameDelegator;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.network_layer.EthernetPacketSender;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
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

	FrameSender endPoint;
	FrameDelegator frameDelegator;
    ARPController arpController;
	
    PacketSender packetSender;
	ProtocolDelegator<IPPacket> packetDelegator;
    
	ICMPPacketReceiver icmpPacketReceiver;	
	PingController pingController;
    UDPController udpController;
    

    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.frameDelegator = new FrameDelegator(name);
        this.endPoint = new NICCard(name, this.frameDelegator);
        this.arpController = new ARPController(this,endPoint);
        this.packetSender = new EthernetPacketSender(this, arpController, endPoint);
    }
    
    // Commands available to the user of the PC

	public void ping(IPAddress ipAddress2, int i) {
		pingController.sendPingRequest(ipAddress2, i);
	}

	public void traceroute(IPAddress ipAddress2) {
		pingController.sendTraceroute(ipAddress2);
	}

	// System commands available application code
	
	public void bind(DatagramSocket socket, IPAddress multicastAddress, short portNumber) {
		udpController.bind(multicastAddress, portNumber, socket);		
	}

	public void display(String string) {
		System.out.println(string);
	}

	// General information about the PC
	
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
    
	public IPAddress getMulticastAddress() {
		return multiCastIP;
	}

	// system calls
	
	public void setMulticastAddress(IPAddress multiCastIP) {
		this.multiCastIP = multiCastIP;
		this.arpController.put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		this.ipAddress = ip;
	}

    // Setters and Getters for various components
    
    public void setEndPoint(FrameSender endPoint) {
		this.endPoint = endPoint;
	}

    public ARPController getArpController() {
        return arpController;
    }
    
    public PhysicalEndPoint getEndPoint() {
        return (PhysicalEndPoint) endPoint;
    }
    
    public FrameDelegator getFrameDelegator() {
		return frameDelegator;
	}

	public FrameReceiver getFrameReceiver() {
		return frameDelegator;
	}
	
	public FrameSender getFrameSender() {
		return endPoint;
	}
	
	public PacketSender getPacketSender() {
		return packetSender;
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

	public void setPingController(PingController newInstance) {
		this.pingController = newInstance;		
	}
	
	public PingController getPingController() {
		return pingController;
	}

	public void setIPDelegator(ProtocolDelegator<IPPacket> delegator) {
		this.packetDelegator = delegator;
	}
	
	public ProtocolDelegator<IPPacket> getIPDelegator() {
		return packetDelegator;
	}
}
