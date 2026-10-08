package org.singsurf.jns3.physical_layer;

/**
 * Represents a physical ethernet connection between two devices.
 * Frames sent by one device are received by the other.
 */
public interface PhysicalConnection {
    /**
     * Sends an Ethernet frame from the sender device 
     * to the other device.
     * The second parameter represents the sender device ensures
     * the frame is sent sent to  the correct device.
     * It should usually be called as 
     * {@code connection.send(frame, this);}
     * @param frame the Ethernet frame to be sent
     * @param sender the device sending the frame
     */
    void send(byte[] frame);
}
