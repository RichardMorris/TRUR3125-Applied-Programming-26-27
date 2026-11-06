package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.generic_protocol.HandlerStatus;

public class IPFrameHandler implements FrameHandler {

	private IPProtocol ipProtocol;

	public IPFrameHandler(IPProtocol ipProtocol) {
		this.ipProtocol = ipProtocol;
	}

	@Override
	public HandlerStatus handle(EthernetFrame frame) {
		var payload = frame.getContents();
		if(payload.length == 0) {
			return HandlerStatus.CONTINUE;
		}
		if(payload[0]  != IPPacket.IPV4_ID) {
			return HandlerStatus.CONTINUE;
		}
		if(payload.length < 20) {
			return new HandlerStatus.Invalid("IP packet too short: " + payload.length);
		}
		var packet = IPPacketExtractor.getInstance().extract(frame);
		ipProtocol.delegator.receive(packet);
		return HandlerStatus.COMPLETED;
	}

}
