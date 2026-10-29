package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.EthernetProtocol;
import org.singsurf.jns3.data_link_layer.FrameDelegator;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
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

	PhysicalEndPoint endPoint;
	FrameSender frameSender;
	
	ProtocolDelegator<IPPacket> packetDelegator;
    
//	ICMPPacketReceiver icmpPacketReceiver;	
//	PingController pingController;
    UDPController udpController;
    
	EthernetProtocol ethernetProtocol;
    ARPProtocol arpProtocol;
	IPProtocol ipProtocol;
	ICMPProtocol icmpProtocol;

    public void setIcmpProtocol(ICMPProtocol icmpProtocol) {
		this.icmpProtocol = icmpProtocol;
	}

	public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }
    
	public void setEthernetProtocol(EthernetProtocol ethernetProtocol) {
		this.ethernetProtocol = ethernetProtocol;
        this.endPoint = ethernetProtocol.getEndPoint();
        this.frameSender = ethernetProtocol.getFrameSender();
	}

	
    // Commands available to the user of the PC

	public void setArpProtocol(ARPProtocol arpProtocol) {
		this.arpProtocol = arpProtocol;
	}

	public ARPProtocol getArpProtocol() {
		return arpProtocol;
	}

	public void ping(IPAddress ipAddress2, int i) {
		icmpProtocol.sendPingRequest(ipAddress2, i);
	}

	public void traceroute(IPAddress ipAddress2) {
		icmpProtocol.sendTraceroute(ipAddress2);
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
		arpProtocol.getArpController().put(multiCastIP, MACAddress.BROADCAST_MAC);
	}

	public void setIPAddress(IPAddress ip) {
		this.ipAddress = ip;
	}

    // Setters and Getters for various components
    
    public void setEndPoint(PhysicalEndPoint endPoint) {
		this.endPoint = endPoint;
	}
    
    public PhysicalEndPoint getEndPoint() {
        return (PhysicalEndPoint) endPoint;
    }
    
    public FrameDelegator getFrameDelegator() {
		return ethernetProtocol.getFrameDelegator();
	}

	public FrameReceiver getFrameReceiver() {
		return ethernetProtocol.getFrameReceiver();
	}
	
	public FrameSender getFrameSender() {
		return ethernetProtocol.getFrameSender();
	}
	
	public PacketSender getPacketSender() {
		return ipProtocol.getPacketSender();
	}
	
//	public void setICMPHandler(ICMPPacketReceiver icmpPacketReceiver) {
//		this.icmpPacketReceiver = icmpPacketReceiver;
//	}
//	
//	public ICMPPacketReceiver getICMPHandler() {
//		return icmpPacketReceiver;
//	}

	public void setUDPController(UDPController udpController2) {
		this.udpController = udpController2;
	}

	public UDPController getUDPController() {
		return udpController;
	}

//	public void setPingController(PingController newInstance) {
//		this.pingController = newInstance;		
//	}
//	
//	public PingController getPingController() {
//		return pingController;
//	}

//	public void setIPDelegator(ProtocolDelegator<IPPacket> delegator) {
//		this.packetDelegator = delegator;
//	}
	
	public ProtocolDelegator<IPPacket> getIPDelegator() {
		return ipProtocol.getDelegator();
	}

	public void setIPProtocol(IPProtocol ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	public Terminal getTerminal() {
		return this;
	}

}
