package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPMessage;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.icmp.ICMPErrorMessage;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;

/**
 * A PassthroughRouter is a simple router that forwards Ethernet frames between its two ports.
 * If the frame is an ARPMessage then forwarded to the other port,
 * hence the router is invisible to ARP. 
 * If the frame is an IPPacket then 
 * 
 *  otherwise it is dropped. This is a simple implementation of a router that does not perform any routing logic.
 */
public class PassthroughRouter extends MultiPortDevice {
    List<FrameHandler> frameHandlers = new ArrayList<>();
    IPAddress address;
    
    FrameFactory frameFactory = new FrameFactory();

    public PassthroughRouter(String name, IPAddress address) {
		super(name, 2);
		this.address = address;
	}

	@Override
	public void receiveFrame(int portNumber, EthernetFrame frame) {
		if(ARPMessage.isARPMessage(frame)) {
			sendFrame(1 - portNumber, frame);
			return;
		}
		var payload = frame.getPayload();
		if(IPPacket.isIPPacket(payload)) {
			var packet = IPPacket.fromBytes(payload);
			packet.decrementTTL();
			if(packet.getTimeToLive() > 0) {
				sendFrame(1 - portNumber, frame);
				return;
			}				
			if(ICMPMessage.isICMPMessage(packet)) {
				var icmp = ICMPMessage.fromPacket(packet);
				if(icmp.isPingRequest()) {
					var errorMsg = ICMPErrorMessage.fromPacket(packet);
					var errorPacket = new IPPacket(
						IPPacket.IMCP, 64,
						address, packet.getSourceAddress(), 
						errorMsg.getBytes());
					var errorFrame = frameFactory.createEthernetFrame(
						frame.getSource(),frame.getDestination(), errorPacket.toBytes());

					sendFrame(portNumber,errorFrame);
					return;
				} else {
					Jns3Logger.logError(name, "Unknown ICMP message type "+icmp);
					return;
				}
			}
			Jns3Logger.logInfo(name, "Dropped packet "+packet.toString());
			return;
		}
	}

}
