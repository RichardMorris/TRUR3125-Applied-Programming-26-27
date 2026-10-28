package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolSender;

/**
 * Interface for classes that can send Ethernet frames.
 */
public interface FrameSender extends ProtocolSender<EthernetFrame> {

    void send(EthernetFrame frame);
}
