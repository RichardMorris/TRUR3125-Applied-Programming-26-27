package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

/**
 * A Port on an @link MultiPortDevice.
 * 
 * Since week 10 it buffers output.
 */
public class Port implements NamedDevice, FrameReceiver, FrameSender {
    MultiPortDevice hostDevice;
    int portNumber;
    NICCard ethernetEndPoint;
    String name;

    public Port(MultiPortDevice hostDevice, int portNumber) {
        this.name = hostDevice.getName() + "::" + portNumber;
        this.hostDevice = hostDevice;
        this.portNumber = portNumber;
        this.ethernetEndPoint = new NICCard(getName(), this); 
    }

    @Override
    public void receive(EthernetFrame frame) {
        hostDevice.receiveFrame(portNumber, frame);
    }

    public PhysicalEndPoint getEthernetEndPoint() {
        return ethernetEndPoint;
    }

    public boolean isConnected() {
        return ethernetEndPoint.isConnected();
    }
    @Override
    public void send(EthernetFrame frame) {
        ethernetEndPoint.send(frame);
    }   

    @Override
    public String getName() {
        return name;
    }
}
