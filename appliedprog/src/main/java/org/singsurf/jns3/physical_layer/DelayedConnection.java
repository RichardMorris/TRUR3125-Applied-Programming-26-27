package org.singsurf.jns3.physical_layer;

/**
 * A delayed connection between two devices.
 * Every frame is delayed by 1ms. Has no practical 
 * in a single threaded environment.
 */
public class DelayedConnection extends PhysicalConnection {
    int delay;

    public DelayedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this(dev1, dev2, 1);
    }

    public DelayedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, int delay) {
        super(dev1, dev2);
        this.delay = delay;
    }

    @Override
    public void send(byte[] frame, boolean reversed) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        receive(frame, reversed);
    }
}
