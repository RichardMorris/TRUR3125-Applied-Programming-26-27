package org.singsurf.jns3.data_link_layer;

public interface FrameReceiver {
    void receiveFrame(EthernetFrame frame);
}
