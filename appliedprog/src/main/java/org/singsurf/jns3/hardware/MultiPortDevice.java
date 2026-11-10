package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.EthernetFrame;

public abstract class MultiPortDevice extends NamedDevice {
    int numPorts;
    Port[] ports;
    public MultiPortDevice(String name, int numPorts) {
        super(name);
        this.numPorts = numPorts;
        this.ports = new Port[numPorts];
        for (int i = 0; i < numPorts; i++) {
            this.ports[i] = new Port(this, i);
        }
    }

    public Port getPort(int portNumber) {
        if (portNumber < 0 || portNumber >= numPorts) {
            throw new IllegalArgumentException("Invalid port number: " + portNumber);
        }
        return ports[portNumber];
    }
    
    public abstract void receiveFrame(int portNumber, EthernetFrame frame);
}
