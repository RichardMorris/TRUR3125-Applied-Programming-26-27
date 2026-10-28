package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolReceiver;

/**
 * Interface for classes that can receive Ethernet frames.
 */
public interface FrameReceiver extends ProtocolReceiver<EthernetFrame> {
    void receive(EthernetFrame frame);
}
