package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;

public class Port extends NamedDevice implements FrameReceiver, FrameSender {
    MultiPortDevice hostDevice;
    int portNumber;
    EthernetEndPoint ethernetEndPoint;

    public Port(MultiPortDevice hostDevice, int portNumber) {
        super(hostDevice.getName() + "::" + portNumber);
        this.hostDevice = hostDevice;
        this.portNumber = portNumber;
        this.ethernetEndPoint = new EthernetEndPoint(getName(), this); 
    }

    @Override
    public void receiveFrame(EthernetFrame frame) {
        hostDevice.receiveFrame(portNumber, frame);
    }

    public EthernetEndPoint getEthernetEndPoint() {
        return ethernetEndPoint;
    }

    public boolean isConnected() {
        return ethernetEndPoint.isConnected();
    }
    @Override
    public void sendFrame(EthernetFrame frame) {
        ethernetEndPoint.sendFrame(frame);
    }   
}
