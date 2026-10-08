package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.NamedDevice;
import org.singsurf.jns3.physical_layer.EthernetConnection;

public abstract class EthernetEndPoint extends NamedDevice {
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

    public void sendFrame(EthernetFrame frame) {
        if (connection != null) {
            connection.send(frame);
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

    public abstract void receiveFrame(EthernetFrame frame);

}
