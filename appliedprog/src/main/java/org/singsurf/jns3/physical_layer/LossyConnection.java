package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;

/**
 * A lossy connection between two devices.
 * Every 10th frame is dropped.
 */
public class LossyConnection implements EthernetConnection {
    EthernetEndPoint dev1;
    EthernetEndPoint dev2;
    private int count = 0;

    public LossyConnection(EthernetEndPoint dev1, EthernetEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private LossyConnection(EthernetEndPoint dev1, EthernetEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(EthernetFrame frame) {
        count++;
        if (count % 10 != 0) {
            dev2.receiveFrame(frame);
        }
    }

    public EthernetConnection getReverseConnection() {
        return new LossyConnection(dev2, dev1, true);
    }
}
