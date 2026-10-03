package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;

public class PC implements FrameReceiver {
    private MACAddress macAddress;
    private String name;
    
    public PC(String name, MACAddress macAddress) {
        this.name = name;
        this.macAddress = macAddress;
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    @Override
    public void receiveFrame(EthernetFrame frame) {
        if(frame.getDestination().equals(this.macAddress)) {
            Jns3Logger.log(name, Jns3Logger.LogLevel.RECEIVED,  
                FrameFactory.decode(frame));
        } else {
            Jns3Logger.log(name, Jns3Logger.LogLevel.IGNORED,  
                FrameFactory.decode(frame));
        }
    }
}
