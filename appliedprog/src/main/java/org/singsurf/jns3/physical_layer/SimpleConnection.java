package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.hardware.Device;
import org.singsurf.jns3.data_link_layer.EthernetFrame;

/**
 * A simple connection between two devices.
 * Frames sent by one device are received by the other.
 */
public class SimpleConnection implements Connection {
    Device dev1;
    Device dev2;

    public SimpleConnection(Device dev1, Device dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
    }

    public void send(EthernetFrame frame) {
        dev2.receiveFrame(frame);
    }

    public Connection getReverseConnection() {
        return new SimpleConnection(dev2, dev1);
    }
}
