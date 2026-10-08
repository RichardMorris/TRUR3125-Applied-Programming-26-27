package org.singsurf.jns3.physical_layer;

/**
 * A lossy connection between two devices.
 * Every 10th frame is dropped.
 */
public class LossyConnection implements PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;
    private int count = 0;

    public LossyConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private LossyConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(byte[] frame) {
        count++;
        if (count % 10 != 0) {
            dev2.receive(frame);
        }
    }

    public PhysicalConnection getReverseConnection() {
        return new LossyConnection(dev2, dev1, true);
    }
}
