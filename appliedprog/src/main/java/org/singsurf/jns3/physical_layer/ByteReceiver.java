package org.singsurf.jns3.physical_layer;

public interface ByteReceiver {

    /**
     * Receives a frame from the connection.
     * @param frame the data being received
     */
    void receive(byte[] frame);

}
