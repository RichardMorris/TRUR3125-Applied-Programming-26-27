package org.singsurf.jns3.data_link_layer;

/**
 * Interface for classes that can send Ethernet frames.
 */
public interface FrameSender {

    void sendFrame(EthernetFrame frame);
}
