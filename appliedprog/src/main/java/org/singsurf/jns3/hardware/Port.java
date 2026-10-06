package org.singsurf.jns3.hardware;


import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.physical_layer.EthernetConnection;
import org.singsurf.jns3.physical_layer.EthernetEndPoint;

public class Port extends EthernetEndPoint {
    MultiPortDevice hostDevice;
    int portNumber;
    EthernetConnection connection;

    public Port(MultiPortDevice hostDevice, int portNumber) {
        super(hostDevice.getName() + "::" + portNumber);
        this.hostDevice = hostDevice;
        this.portNumber = portNumber;
    }

    public void setConnection(EthernetConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

	@Override
	public void sendFrame(EthernetFrame frame) {
        if(connection == null) {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection available on port " + portNumber);
            return;
        }
        connection.send(frame);
    }

    @Override
    public void receiveFrame(EthernetFrame frame) {
        hostDevice.receiveFrame(portNumber, frame);
    }

}
