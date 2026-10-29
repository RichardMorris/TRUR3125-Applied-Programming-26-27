package org.singsurf.jns3.network_layer.icmp;

import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.Terminal;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class ICMPProtocol implements LowerAdapter<IPPacket> {

	ICMPPacketReceiver icmpPacketReceiver;
	PingController pingController;
	PingRequestHandler pingRequestHandler;
	
	public ICMPProtocol(AddressableDevice device, Terminal terminal) {
		icmpPacketReceiver = new ICMPPacketReceiver(device.getName());
		pingController = PingController.newInstance(device, terminal);
		icmpPacketReceiver.addHandler(pingController);
		pingRequestHandler = new PingRequestHandler(device);
		icmpPacketReceiver.addHandler(pingRequestHandler);
		icmpPacketReceiver.addHandler(new ICMPMessageLogger(device.getName()));
	}

	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(icmpPacketReceiver);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<IPPacket> receiver) {
	}

	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		pingController.setPacketSender((PacketSender) sender);
		pingRequestHandler.setSender((PacketSender) sender);
		
	}

	public void sendPingRequest(IPAddress ipAddress2, int i) {
		pingController.sendPingRequest(ipAddress2, i);		
	}

	public void sendTraceroute(IPAddress ipAddress2) {
		pingController.sendTraceroute(ipAddress2);
	}

}
