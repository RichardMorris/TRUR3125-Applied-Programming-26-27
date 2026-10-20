package org.singsurf.jns3.hardware;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPPacket;

public class Switch extends MultiPortDevice {
    Map<Integer,Set<MACAddress>> connectedMacAddress = new HashMap<>();
    Map<MACAddress, Integer> macToPortMap = new HashMap<>();
    public boolean logIPPackets = false;
    
    public Switch(String name, int numPorts) {
        super(name, numPorts);
        for (int i = 0; i < numPorts; i++) {
			connectedMacAddress.put(i, new HashSet<>());
		}
    }

    @Override
    public void receiveFrame(int portNumber, EthernetFrame frame) {
        var sourceMac = frame.getSource();
        if(logIPPackets && IPPacket.isIPPacket(frame.getPayload())) {
        	var packet = IPPacket.fromBytes(frame.getPayload());
			if(logIPPackets) {
				Jns3Logger.logInfo(getName(), ": Received IP packet "+frame+" "+packet);
			}
		}
        connectedMacAddress.get(portNumber).add(sourceMac);
        macToPortMap.put(sourceMac, portNumber);
        var destinationMac = frame.getDestination();

        var destinationPort = macToPortMap.get(destinationMac);
        if (destinationPort != null) { // If the destination MAC address is known, forward the frame to the corresponding port
            if (destinationPort != portNumber) {
                sendFrame(destinationPort, frame);
            } else {
                Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR,": Frame received on port " + portNumber + " is destined for the same port.");
            }
        } else { // If the destination MAC address is unknown, broadcast the frame to all other ports
            for (int i = 0; i < numPorts; i++) {
                if (i != portNumber) {
                    sendFrame(i, frame);
                }
            }
        }
    }
}
