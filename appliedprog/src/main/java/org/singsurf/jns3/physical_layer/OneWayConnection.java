package org.singsurf.jns3.physical_layer;

public class OneWayConnection extends PhysicalConnection {
    public OneWayConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        super(dev1, dev2);
    }

    @Override
    protected void send(byte[] frame, boolean reversed) {
        if(!reversed) {
            receive(frame, reversed);
        }
    }
}
