package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;

/**
 * A simple connection between two devices.
 * Frames sent by one device are received by the other.
 */
public class SimpleConnection implements EthernetConnection {
    EthernetEndPoint dev1;
    EthernetEndPoint dev2;

    public SimpleConnection(EthernetEndPoint dev1, EthernetEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private SimpleConnection(EthernetEndPoint dev1, EthernetEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(EthernetFrame frame) {
        dev2.receiveFrame(frame);
    }

    public EthernetConnection getReverseConnection() {
        return new SimpleConnection(dev2, dev1, true);
    }
}
