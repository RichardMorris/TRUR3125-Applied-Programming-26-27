package org.singsurf.jns3.hardware;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.Jns3Logger;

public class Switch extends MultiPortDevice {
    MACAddress[] connectedMacAddress;

    public Switch(String name, int numPorts) {
        super(name, numPorts);
        this.connectedMacAddress = new MACAddress[numPorts];
    }

    @Override
    public void receiveFrame(int portNumber, EthernetFrame frame) {
        var sourceMac = frame.getSource();
        connectedMacAddress[portNumber] = sourceMac;
        var destinationMac = frame.getDestination();

        int destinationPort = -1;
        for (int i = 0; i < numPorts; i++) {
            if (connectedMacAddress[i] != null && connectedMacAddress[i].equals(destinationMac)) {
                destinationPort = i;
                break;
            }
        }

        if (destinationPort != -1) {
            if (destinationPort != portNumber) {
                if(ports[destinationPort].isConnected()) {
                    ports[destinationPort].sendFrame(frame);
                }
                else {
                    Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR,": Port " + destinationPort + " is not connected.");
                }
            } else {
                Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR,": Frame received on port " + portNumber + " is destined for the same port.");
            }
        } else { // If the destination MAC address is unknown, broadcast the frame to all other ports
            for (int i = 0; i < numPorts; i++) {
                if (i != portNumber) {
                    if (ports[i].isConnected()) {
                        ports[i].sendFrame(frame);
                    }
                }
            }
        }
    }
}
