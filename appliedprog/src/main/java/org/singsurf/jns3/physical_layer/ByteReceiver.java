package org.singsurf.jns3.physical_layer;

/**
 * Receives arrays of bytes from a connection.
 */
public interface ByteReceiver {

    /**
     * Receives a frame from the connection.
     * @param frame the data being received
     */
    void receive(byte[] frame);

}
