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
import org.singsurf.jns3.data_link_layer.MACAddressable;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPAddressable;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

public class PC implements NamedDevice, MACAddressable, IPAddressable, FrameSender, FrameReceiver {
    String name;
    List<FrameHandler> frameHandlers = new ArrayList<>();
    ARPController arpController;
    FrameFactory frameFactory;
    MACAddress macAddress;
    IPAddress ipAddress;
    EthernetEndPoint ethernetEndPoint;

    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.ethernetEndPoint = new EthernetEndPoint(name, this);
        this.frameFactory = new FrameFactory();
        this.arpController = new ARPController(this);
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
        return ethernetEndPoint;
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

}
