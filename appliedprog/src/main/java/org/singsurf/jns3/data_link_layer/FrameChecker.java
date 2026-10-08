package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.NamedDevice;

public class FrameChecker implements FrameHandler{
    NamedDevice device;
    public FrameChecker(NamedDevice device) {
        this.device = device;
    }
    /**
     * Checks if the given Ethernet frame has a valid frame check sequence (CRC).
     * @param frame The Ethernet frame to be checked.
     * @return true if the frame has an invalid CRC, false otherwise.
     */
    @Override
    public boolean handleFrame(EthernetFrame frame) {
        long crc = frame.getFrameCheck();
        long crc2 = frame.calcFrameCheck();
        if(crc != crc2) {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.ERROR,
                "Detected invalid CRC in frame");
            return true;
        }
        return false;
    }

}
