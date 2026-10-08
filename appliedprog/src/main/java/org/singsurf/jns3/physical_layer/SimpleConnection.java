package org.singsurf.jns3.physical_layer;

/**
 * A simple connection between two devices.
 * Frames sent by one device are received by the other.
 */
public class SimpleConnection implements PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;

    public SimpleConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private SimpleConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(byte[] frame) {
        dev2.receive(frame);
    }

    public PhysicalConnection getReverseConnection() {
        return new SimpleConnection(dev2, dev1, true);
    }
}
