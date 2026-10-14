package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.physical_layer.BufferedEndPoint;

public class NICCard extends BufferedEndPoint implements FrameSender {

	private FrameReceiver frameReceiver;

	public NICCard(String name, FrameReceiver frameReceiver) {
		super(name);
		this.frameReceiver = frameReceiver;
	}
	
    @Override
    public void sendFrame(EthernetFrame frame) {
        send(frame.getBytes());
    }   

    @Override
    protected void receive(byte[] frame) {
        EthernetFrame ethernetFrame = EthernetFrame.parseBytes(frame);
        frameReceiver.receiveFrame(ethernetFrame);
    }


}
