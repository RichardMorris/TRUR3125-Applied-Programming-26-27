package org.singsurf.jns3.data_link_layer;

public interface FrameHandler {
    /**
     * Attempt to handle the given frame. Returns true if the frame was successfully handled, false otherwise.
     * @param frame The frame to be handled.
     * @return true if the frame was successfully handled, false otherwise.
     */
    boolean handleFrame(EthernetFrame frame);
}
