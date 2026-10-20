package org.singsurf.jns3.hardware;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.EthernetPacketSender;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketReceiver;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPPacketReceiver;
import org.singsurf.jns3.network_layer.icmp.PingController;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.transport_layer.udp.DatagramSocket;
import org.singsurf.jns3.transport_layer.udp.UDPController;

public class PC implements AddressableDevice, FrameSender, FrameReceiver, PacketSender {
    String name;
    List<FrameHandler> frameHandlers = new ArrayList<>();
    ARPController arpController;
    FrameFactory frameFactory;
    MACAddress macAddress;
    IPAddress ipAddress;
    FrameSender ethernetEndPoint;
    PacketReceiver packetReceiver;
    PacketSender packetSender;
    UDPController udpController;
	IPAddress multiCastIP;
	ICMPPacketReceiver icmpPacketReceiver;
	PingController pingController;

    
    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.ethernetEndPoint = new EthernetEndPoint(name, this);
        this.frameFactory = new FrameFactory();
        this.arpController = new ARPController(this,this);
        this.packetSender = new EthernetPacketSender(this, arpController, ethernetEndPoint);
    }

    @Override
    public void sendFrame(EthernetFrame frame) {
        ethernetEndPoint.sendFrame(frame);
    }

    public void receiveFrame(EthernetFrame frame) {
        for (FrameHandler handler : frameHandlers) {
            if (handler.handleFrame(frame)) {
                return;
            }
        }
        Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "Unhandled frame: " + frame);
    }

    public void addFrameHandler(FrameHandler frameHandler) {
        frameHandlers.add(frameHandler);
    }

    public void sendGARPRequest() {
        arpController.sendGARPRequest();
    }

    public void sendArpRequest(IPAddress targetIP) {
        arpController.sendArpRequest(targetIP);
    }

    public EthernetFrame createEthernetFrame(MACAddress destinationMac, String payload) {
        return frameFactory.createEthernetFrame(destinationMac, getMACAddress(), payload);
    }

    public EthernetFrame createEthernetFrame(MACAddress destinationMac, byte[] payload) {
        return frameFactory.createEthernetFrame(destinationMac, getMACAddress(), payload);
    }

    public MACAddress lookup(IPAddress ipAddress) {
        return arpController.getArpTable().get(ipAddress);
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
    
    public void setEndPoint(FrameSender endPoint) {
		this.ethernetEndPoint = endPoint;
	}

    public FrameFactory getFrameFactory() {
        return frameFactory;
    }

    public ARPController getArpController() {
        return arpController;
    }

	public void clearFrameHandlers() {
		frameHandlers.clear();		
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
	
	public IPPacket createIPPacket(int protocol,IPAddress dest, byte[] data) {
        IPPacket packet = new IPPacket(protocol, 64,
                getIPAddress(), dest, data);
		return packet;
	}

	public IPPacket createIPPacket(int protocol,IPAddress dest, String data) {
        IPPacket packet = new IPPacket(protocol, 64,
                getIPAddress(), dest, data.getBytes());
		return packet;
	}

	public void sendPacket(IPPacket packet) {
		packetSender.sendPacket(packet);
	}

	public void display(String string) {
		System.out.println(string);
	}

	public void bind(DatagramSocket socket, IPAddress multicastAddress, short s) {
		udpController.bind(multicastAddress, s, socket);
		
	}
	

	public IPAddress getMulticastAddress() {
		return multiCastIP;
	}

	public void setMulticastAddress(IPAddress multiCastIP) {
		this.multiCastIP = multiCastIP;
		this.arpController.put(multiCastIP, new MACAddress("FF:FF:FF:FF:FF:FF"));
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

	public void sendPingRequest(IPAddress ipAddress2, int i) {
		pingController.sendPingRequest(ipAddress2, i);
	}

	public void setPingController(PingController newInstance) {
		this.pingController = newInstance;		
	}
	
	public PingController getPingController() {
		return pingController;
	}

	public void sendTraceroute(IPAddress ipAddress2) {
		pingController.sendTraceroute(ipAddress2);
	}
}
