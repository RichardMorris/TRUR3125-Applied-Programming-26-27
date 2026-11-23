package org.singsurf.jns3.physical_layer;

/**
 * A simple connection between two devices.
 * Frames sent by one device are received by the other.
 */
public class SimpleConnection extends PhysicalConnection {

    public SimpleConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        super(dev1, dev2);
    }

    @Override
    public void send(byte[] frame, boolean reversed) {
        receive(frame, reversed);
    }
}
