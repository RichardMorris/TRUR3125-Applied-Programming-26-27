package org.singsurf.jns3.network_layer.icmp;

import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

/**
 * Implements the ICMP protocol,
 *  including ping, traceroute and handling TTL expired messages.
 *   sending and receiving, 
 * ping requests, and delegation to handlers.
 */
public class ICMPProtocol implements Protocol, LowerAdapter<IPPacket> {
	ProtocolDevice device;
	String name;
	ICMPPacketReceiver icmpPacketReceiver;
	PingController pingController;
	PingRequestHandler pingRequestHandler;
	
	public ICMPProtocol(ProtocolDevice device) {
		this.device = device;
		this.name = device.getName() + "-ICM";
		icmpPacketReceiver = new ICMPPacketReceiver(device.getName());
		pingController = PingController.newInstance(device);
		icmpPacketReceiver.addHandler(pingController);
		pingRequestHandler = new PingRequestHandler(device);
		icmpPacketReceiver.addHandler(pingRequestHandler);
		icmpPacketReceiver.addHandler(new ICMPMessageLogger(device.getName()));
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<IPPacket> receiver) {
	}

	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		pingController.setPacketSender((PacketSender) sender);
		pingRequestHandler.setSender((PacketSender) sender);
		
	}

	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(icmpPacketReceiver);
	}

	public void sendPingRequest(IPAddress ipAddress2, int i) {
		pingController.sendPingRequest(ipAddress2, i);		
	}

	public void sendTraceroute(IPAddress ipAddress2) {
		pingController.sendTraceroute(ipAddress2);
	}

	public List<PingController.PingReplyRecord> getCompletedPings() {
		return pingController.getCompletedPings();
	}
	
	public List<PingController.PingReplyRecord> getWarnings() {
		return pingController.getWarnings();
	}
	
	@Override
	public String getName() {
		return name;
	}
}
