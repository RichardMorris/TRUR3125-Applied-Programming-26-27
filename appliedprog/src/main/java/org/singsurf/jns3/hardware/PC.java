package org.singsurf.jns3.hardware;
import org.singsurf.jns3.physical_layer.EthernetEndPoint;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.MACAddressable;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPAddressable;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.ARPController;


public class PC extends EthernetEndPoint implements MACAddressable, IPAddressable {
    List<FrameHandler> frameHandlers = new ArrayList<>();
    ARPController arpController;
    FrameFactory frameFactory;
    MACAddress macAddress;
    IPAddress ipAddress;

    @Override
    public MACAddress getMACAddress() {
        return macAddress;
    }

    @Override
    public IPAddress getIPAddress() {
        return ipAddress;
    }

    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name);
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }


    public void addFrameHandler(FrameHandler frameHandler) {
        frameHandlers.add(frameHandler);
    }


    public void receiveFrame(EthernetFrame frame) {
        for (FrameHandler handler : frameHandlers) {
            if (handler.handleFrame(frame)) {
                return;
            }
        }
        Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "Unhandled frame: " + frame);
    }


    public ARPController getArpController() {
        return arpController;
    }

    public void setArpController(ARPController arpController) { 
        this.arpController = arpController;
        frameHandlers.add(arpController);
    }   

    public void sendGARPRequest() {
        arpController.sendGARPRequest();
    }

    public void sendArpRequest(IPAddress targetIP) {
        arpController.sendArpRequest(targetIP);
    }

    public FrameFactory getFrameFactory() {
        return frameFactory;
    }

    public void setFrameFactory(FrameFactory frameFactory) {
        this.frameFactory = frameFactory;
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
}
