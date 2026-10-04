package org.singsurf.jns3.transport_layer;

import org.singsurf.jns3.data_link_layer.Device;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;

/**
 * Represents a physical connection between two devices.
 * Frames sent by one device are received by the other.
 */
public interface Connection {
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
    void send(EthernetFrame frame, Device sender);

}
