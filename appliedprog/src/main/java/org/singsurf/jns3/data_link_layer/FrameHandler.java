package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.ProtocolHandler;

/**
 * Interface defining handlers for Ethernet frames.
 */
public interface FrameHandler extends ProtocolHandler<EthernetFrame> {
    /**
     * Attempt to handle the given frame. Returns true if the frame was successfully handled, false otherwise.
     * @param frame The frame to be handled.
     * @return true if the frame was successfully handled, false otherwise.
     */
//    boolean handleFrame(EthernetFrame frame);
}
