package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;

/**
 * A delayed connection between two devices.
 * Every frame is delayed by 1ms. Has no practical 
 * in a single threaded environment.
 */
public class DelayedConnection implements EthernetConnection {
    EthernetEndPoint dev1;
    EthernetEndPoint dev2;

    public DelayedConnection(EthernetEndPoint dev1, EthernetEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private DelayedConnection(EthernetEndPoint dev1, EthernetEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(EthernetFrame frame) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        dev2.receiveFrame(frame);
    }

    public EthernetConnection getReverseConnection() {
        return new DelayedConnection(dev2, dev1, true);
    }
}
