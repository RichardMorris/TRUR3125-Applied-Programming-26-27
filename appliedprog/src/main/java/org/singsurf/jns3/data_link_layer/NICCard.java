package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.physical_layer.BufferedEndPoint;

/**
 * Network Interface Card: a buffering adapter between the physical layer and the data link layer.
 */
public class NICCard extends BufferedEndPoint implements FrameSender {

	private FrameReceiver frameReceiver;
	ByteToFrameConverter converter = new ByteToFrameConverter();

	public NICCard(String name, FrameReceiver frameReceiver) {
		super(name);
		this.frameReceiver = frameReceiver;
	}
	
    @Override
    public void send(EthernetFrame frame) {
        send(frame.getBytes());
    }   

    @Override
    protected void receive(byte[] frame) {
        EthernetFrame ethernetFrame = converter.fromBytes(frame);
        frameReceiver.receive(ethernetFrame);
    }


}
