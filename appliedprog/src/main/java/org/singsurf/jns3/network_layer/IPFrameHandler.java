package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;

/**
 * Handles Ethernet frames that contain IP packets and passes them to a ProtocolReceiver.
 */
public class IPFrameHandler implements FrameHandler {
	ProtocolReceiver<IPPacket> receiver;

	public IPFrameHandler(ProtocolReceiver<IPPacket> receiver) {
		this.receiver = receiver;
	}

	@Override
	public HandlerStatus handle(EthernetFrame frame) {
		if(frame.getEtherType() != IPProtocol.ETHERTYPE) {
			return HandlerStatus.CONTINUE;
		}
		var payload = frame.getContents();
		if(payload.length < 20) {
			return new HandlerStatus.Invalid("IP packet too short: " + payload.length);
		}
		if(payload[0]  != IPPacket.IPV4_ID) {
			var byteStr = String.format("%02X", payload[0]);
			return new HandlerStatus.Invalid("Frame with IP EtherType but wrong version/headerlength: " + byteStr);
		}
		var packet = IPPacketExtractor.getInstance().extract(frame);
		receiver.receive(packet);
		return HandlerStatus.COMPLETED;
	}

}
