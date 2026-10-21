package org.singsurf.jns3.hardware;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.physical_layer.PhysicalEndPoint;

public abstract class MultiPortDevice implements NamedDevice {
    String name;
    int numPorts;
    Port[] ports;

    public MultiPortDevice(String name, int numPorts) {
        this.name = name;
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

    public PhysicalEndPoint getEndPoint(int portNumber) {
        return ports[portNumber].ethernetEndPoint;
    }

    public String getName() {
        return name;
    }

    public void sendFrame(int portNumber, EthernetFrame frame) {
        if (ports[portNumber].isConnected()) {
                    ports[portNumber].sendFrame(frame);
        }
    }

    public abstract void receiveFrame(int portNumber, EthernetFrame frame);
}
