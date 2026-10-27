package org.singsurf.jns3.data_link_layer;

/**
 * Interface for classes that can receive Ethernet frames.
 */
public interface FrameReceiver {
    void receiveFrame(EthernetFrame frame);
}
