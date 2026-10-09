package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.Jns3Logger;

public abstract class PhysicalEndPoint extends NamedDevice {
    PhysicalConnection connection;
    boolean reversed;

    public PhysicalEndPoint(String name) {
        super(name);
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
     * Sends a frame through the connection.
     * If no connection is set, logs an error.
     * @param frame the data to be sent
     */
    protected void send(byte[] frame) {
        if (isConnected()) {
            getConnection().send(frame, getReversed());
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

    /**
     * Receives a frame from the connection.
     * @param frame the data being received
     */
    protected abstract void receive(byte[] frame);

}   