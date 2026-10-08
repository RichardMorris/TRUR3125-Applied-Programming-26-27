package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.physical_layer.PhysicalEndPoint;
import org.singsurf.jns3.Jns3Logger;

public abstract class EthernetEndPoint extends PhysicalEndPoint {

    public EthernetEndPoint(String name) {
        super(name);
    }

    public void sendFrame(EthernetFrame frame) {
        if (getConnection() != null) {
            getConnection().send(frame.getBytes());
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

    public void receive(byte[] frame) {
        EthernetFrame ethernetFrame = EthernetFrame.parseBytes(frame);
        receiveFrame(ethernetFrame);
    }

    public abstract void receiveFrame(EthernetFrame frame);
}
