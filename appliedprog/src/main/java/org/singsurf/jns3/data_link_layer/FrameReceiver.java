package org.singsurf.jns3.data_link_layer;

public interface FrameReceiver {
    public void receiveFrame(EthernetFrame frame);
}
