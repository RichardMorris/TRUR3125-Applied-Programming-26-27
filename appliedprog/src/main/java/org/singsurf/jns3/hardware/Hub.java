package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.EthernetFrame;

/**
 * A simple layer-1 Hub that forwards Ethernet frames received on one port to all other ports.
 * 
 * See <a href="../../../../../../Week6.md">Week 6</a> for details on Hub's and Switches.
 */
public class Hub extends MultiPortDevice {
    public Hub(String name, int numPorts) {
        super(name, numPorts);
    }
    
    @Override
    public void receiveFrame(int portNumber, EthernetFrame frame) {
        for (int i = 0; i < numPorts; i++) {
            if (i != portNumber) {
                sendFrame(i, frame);
            }
        }
    }
}
