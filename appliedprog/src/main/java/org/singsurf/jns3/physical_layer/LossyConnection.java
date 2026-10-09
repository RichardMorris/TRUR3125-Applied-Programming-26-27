package org.singsurf.jns3.physical_layer;

/**
 * A lossy connection between two devices.
 * Every 10th frame is dropped.
 */
public class LossyConnection extends PhysicalConnection {
    private int count = 0;

    public LossyConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        super(dev1, dev2);
    }

    @Override
    public void send(byte[] frame, boolean reversed) {
        count++;
        if (count % 10 != 0) {
            receive(frame, reversed);
        }
    }
}
