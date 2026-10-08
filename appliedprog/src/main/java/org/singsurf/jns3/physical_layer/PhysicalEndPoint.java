package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.hardware.NamedDevice;

public abstract class PhysicalEndPoint extends NamedDevice {
    PhysicalConnection connection;

    public PhysicalEndPoint(String name) {
        super(name);
    }

    public PhysicalConnection getConnection() {
        return connection;
    }

    public void setConnection(PhysicalConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public abstract void receive(byte[] frame);

}
