package org.singsurf.jns3.transport_layer;

import org.singsurf.jns3.data_link_layer.FrameTransmitter;
import org.singsurf.jns3.data_link_layer.Device;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.transport_layer.Connection;

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

    public void send(EthernetFrame frame, Device device) {
        if (device == dev1) {
            dev2.receiveFrame(frame);
        } else if (device == dev2) {
            dev1.receiveFrame(frame);
        }
    }

}
