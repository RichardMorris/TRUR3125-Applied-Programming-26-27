package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.NamedDevice;

public abstract class EthernetEndPoint extends NamedDevice implements FrameReceiver, FrameSender {
    EthernetConnection connection;

    public EthernetEndPoint(String name) {
        super(name);
    }

    public EthernetConnection getConnection() {
        return connection;
    }

    public void setConnection(EthernetConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void sendFrame(EthernetFrame frame) {
        if (connection != null) {
            connection.send(frame);
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

}
