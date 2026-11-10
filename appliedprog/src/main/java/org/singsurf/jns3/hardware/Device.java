package org.singsurf.jns3.hardware;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.IPAddress;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;

public abstract class Device implements FrameReceiver, FrameSender {
    private MACAddress macAddress;
    private String name;
    private IPAddress ipAddress;
    FrameFactory frameFactory;
    ARPController arpController;
    List<FrameHandler> frameHandlers = new ArrayList<>();

    public Device(String name, MACAddress macAddress, IPAddress ipAddress) {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
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

    public FrameFactory getFrameFactory() {
        return frameFactory;
    }

    public void setFrameFactory(FrameFactory frameFactory) {
        this.frameFactory = frameFactory;
    }

    public EthernetFrame createEthernetFrame(MACAddress destinationMac, String payload) {
        return frameFactory.createEthernetFrame(destinationMac, macAddress, payload);
    }

    public EthernetFrame createEthernetFrame(MACAddress destinationMac, byte[] payload) {
        return frameFactory.createEthernetFrame(destinationMac, macAddress, payload);
    }

}
