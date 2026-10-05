package org.singsurf.jns3.hardware;

import org.singsurf.jns3.physical_layer.Connection;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.IPAddress;
import org.singsurf.jns3.data_link_layer.MACAddress;

public class PC extends Device {
    Connection connection;

    public PC(String name, MACAddress macAddress, IPAddress ipAddress) {
        super(name, macAddress, ipAddress);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void sendFrame(EthernetFrame frame) {
        if (connection != null) {
            connection.send(frame);
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

    @Override
    public String toString() {
        return "PC\t" + getName() + "\t" + getMacAddress() + "\t" + getIpAddress();
    }
}
