package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.NamedDevice;

/**
 * A FrameHandler that checks the validity of Ethernet frames,
 * error are logged for invalid frames.
 */
public class FrameChecker implements FrameHandler{
    NamedDevice device;
    public FrameChecker(NamedDevice device) {
        this.device = device;
    }
    /**
     * Checks if the given Ethernet frame has a valid frame check sequence (CRC).
     * @param frame The Ethernet frame to be checked.
     * @return Invalid if the frame has an invalid CRC, IGNORED otherwise, as processing should continue.
     */
	@Override
	public HandlerStatus handle(EthernetFrame frame) {
        long crc = frame.getFrameCheck();
        long crc2 = frame.calcFrameCheck();
        if(crc != crc2) {
        	return new HandlerStatus.Invalid("Detected invalid CRC in frame");
        }
		var srcMac = frame.getSource();
		if(srcMac.equals(MACAddress.BROADCAST_MAC)) {
			return new HandlerStatus.Invalid("Received frame with broadcast source MAC address");
		}
		if(srcMac.equals(MACAddress.ZERO_MAC)) {
			return new HandlerStatus.Invalid("Received frame with zero source MAC address");
			}
		return HandlerStatus.CONTINUE;
	}

}
