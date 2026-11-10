package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.Device;

public class DefaultFrameHandler implements FrameHandler {
    Device device;

    public DefaultFrameHandler(Device device) {
        this.device = device;
    }
    @Override
    public boolean handleFrame(EthernetFrame frame) {

        if(frame.getDestination().equals(device.getMacAddress())) {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  
                FrameFactory.decode(frame));
        } else {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.IGNORED,  
                FrameFactory.decode(frame));
        }

        return true;
    }
}
