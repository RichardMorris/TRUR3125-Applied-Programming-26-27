package org.singsurf.jns3.hardware;


import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.physical_layer.EthernetEndPoint;

public class Port extends EthernetEndPoint {
    MultiPortDevice hostDevice;
    int portNumber;

    public Port(MultiPortDevice hostDevice, int portNumber) {
        super(hostDevice.getName() + "::" + portNumber);
        this.hostDevice = hostDevice;
        this.portNumber = portNumber;
    }

    @Override
    public void receiveFrame(EthernetFrame frame) {
        hostDevice.receiveFrame(portNumber, frame);
    }

}
