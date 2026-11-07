package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.ByteToFrameConverter;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.physical_layer.BufferedEndPoint;

/**
 * A Port on an @link MultiPortDevice.
 * 
 * Since week 10 it buffers output.
 */
public class Port implements NamedDevice, FrameReceiver, FrameSender {
    MultiPortDevice hostDevice;
    int portNumber;
    BufferedEndPoint bep;
    String name;
	ByteToFrameConverter converter = new ByteToFrameConverter();

    public Port(MultiPortDevice hostDevice, int portNumber) {
        this.name = hostDevice.getName() + "::" + portNumber;
        this.hostDevice = hostDevice;
        this.portNumber = portNumber;
        this.bep = new BufferedEndPoint(getName()) {

			@Override
			public void receive(byte[] frame) {
				var ethFrame  =converter.fromBytes(frame);
				Port.this.receive(ethFrame);				
			}};
    }

    @Override
    public void receive(EthernetFrame frame) {
        hostDevice.receiveFrame(portNumber, frame);
    }

    public boolean isConnected() {
        return bep.isConnected();
    }
    @Override
    public void send(EthernetFrame frame) {
    	var bytes = frame.getBytes();
        bep.send(bytes);
    }   

    @Override
    public String getName() {
        return name;
    }

}
