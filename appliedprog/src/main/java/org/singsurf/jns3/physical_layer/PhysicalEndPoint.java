package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.hardware.NamedDevice;

/**
 * Represents a socket that a physical connection can be
 * plugged into.
 */
public abstract class PhysicalEndPoint implements NamedDevice {
    PhysicalConnection connection;
    boolean reversed;
    String name;

    public PhysicalEndPoint(String name) {
        this.name = name;
    }

    /**
     * Package private method to set the connection and its reversed state,
     * called from the {@link PhysicalConnection.setConnection} method.
     * @param connection
     * @param reversed
     */

    void setConnection(PhysicalConnection connection, boolean reversed) {
        this.connection = connection;
        this.reversed = reversed;
    }

    public PhysicalConnection getConnection() {
        return connection;
    }

    public boolean getReversed() {
        return reversed;
    }


    public boolean isConnected() {
        return connection != null;
    }

    /**
     * Receives a frame from the connection.
     * @param frame the data being received
     */
    protected abstract void receive(byte[] frame);

    @Override
    public String getName() {
        return name;
    }


}   