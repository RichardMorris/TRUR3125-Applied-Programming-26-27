package org.singsurf.jns3.physical_layer;

/**
 * A delayed connection between two devices.
 * Every frame is delayed by 1ms. Has no practical 
 * in a single threaded environment.
 */
public class DelayedConnection implements PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;
    int delay;

    public DelayedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this(dev1, dev2, 1);
    }

    public DelayedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, int delay) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        this.delay = delay;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private DelayedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(byte[] frame) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        dev2.receive(frame);
    }

    public PhysicalConnection getReverseConnection() {
        return new DelayedConnection(dev2, dev1, true);
    }
}
