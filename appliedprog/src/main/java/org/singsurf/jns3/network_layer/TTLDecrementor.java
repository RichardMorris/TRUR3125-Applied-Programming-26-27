package org.singsurf.jns3.network_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;

public class TTLDecrementor implements PacketReceiver {
	IPProtocol device;
	
	private ProtocolDelegator<IPPacket> delegator;


	public TTLDecrementor(IPProtocol device, ProtocolDelegator<IPPacket> delegator) {
		this.device = device;
		this.delegator = delegator;
	}


	@Override
	public void receive(IPPacket packet) {
		packet.decrementTTL();
		if(packet.getTimeToLive() > 0) {
			delegator.receive(packet);
			return;
		}
		var errorMsg = ICMPMessage.createTTLExpired(packet);
		var errorPacket = new IPPacket(
			IPPacket.IMCP, 64,
			device.device.getIPAddress(), packet.getSourceAddress(), 
			errorMsg.getBytes());
		Jns3Logger.logInfo(device.device.getName(), "Expired TTL "+packet);
		device.getPacketSender().send(errorPacket);
	}

}
