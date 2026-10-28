package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.PC;

/**
 * DefaultFrameHandler that logs a message.
 */
public class DefaultFrameHandler implements FrameHandler {
    PC device;

    public DefaultFrameHandler(PC device) {
        this.device = device;
    }
    
//    @Override
//    public boolean handleFrame(EthernetFrame frame) {
//
//        if(frame.getDestination().equals(device.getMACAddress())) {
//            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  
//                new String(frame.getContents()));
//        } else {
//            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.IGNORED,  
//                new String(frame.getContents()));
//        }
//
//        return true;
//    }

	@Override
	public HandlerStatus handle(EthernetFrame frame) {
        if(frame.getDestination().equals(device.getMACAddress())) {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  
                new String(frame.getContents()));
        } else {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.IGNORED,  
                new String(frame.getContents()));
        }
		return ProtocolHandler.HANDLED;
	}
}
