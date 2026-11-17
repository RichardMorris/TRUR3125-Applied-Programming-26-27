package org.singsurf.jns3.data_link_layer.arp;

import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.ProtocolVisitor;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPProtocol;

/**
 * Implements the Address Resolution Protocol (ARP) for resolving IP addresses to MAC addresses.
 * It handles ARP requests and replies, maintains an ARP table, and provides methods for sending ARP requests.
 */
public class ARPProtocol implements Protocol, LowerAdapter<EthernetFrame> {
	String name;
	ProtocolDevice device;
	boolean isRouter;
	ARPController arpController;
	ARPForwarder arpForwarder;
	/**
	 * EtherType for ARP messages.
	 */
	public static short ARP_ETHER_TYPE = 0x0806;

	public ARPProtocol(ProtocolDevice device, boolean isRouter) {
		name = device.getName() + "-ARP";
		this.device = device;
		this.arpController = new ARPController(this);
		this.isRouter = isRouter;
		arpForwarder = new ARPForwarder(device);
		arpController.put(IPProtocol.LIMITED_MULTICAST_ADDRESS, MACAddress.BROADCAST_MAC);
	}

	@Override
	public List<ProtocolHandler<EthernetFrame>> getInboundHandlers() {
		if (isRouter) {
			return List.of(arpForwarder, arpController);
		}
		return List.of(arpController);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<EthernetFrame> receiver) {
		
	}

	@Override
	public void setOutboundSender(ProtocolSender<EthernetFrame> sender) {
		arpController.setSender((FrameSender) sender);
		arpForwarder.setSender((FrameSender) sender);
	}

	public ARPController getArpController() {
		return arpController;
	}

	public void sendGARPRequest() {
		arpController.sendGARPRequest();
	}

	public void sendArpRequest(IPAddress ip) {
		arpController.sendArpRequest(ip);
	}

	public MACAddress put(IPAddress ip, MACAddress macAddress) {
		return arpController.put(ip, macAddress);
	}

	public MACAddress lookup(IPAddress ipAddress) {
		if(device.getIPAddress().equals(ipAddress)) {
			return device.getMACAddress();
		}
		return arpController.lookup(ipAddress);
	}

	public void announceAddress(IPAddress ipAddress, MACAddress macAddress) {
		arpController.announceAddress(ipAddress, macAddress);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		for (var entry : arpController.arpTable.entrySet()) {
			sb.append("\t").append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
		}
		return sb.toString();
	}

	public void remove(IPAddress ciaddr) {
		arpController.remove(ciaddr);		
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public <R> R accept(ProtocolVisitor<R> visitor, EthernetFrame frame) {
		if(frame.getEtherType() != ARPProtocol.ARP_ETHER_TYPE) return null;

	    var arpMessage = ARPMessage.fromFrame(frame);
	    return visitor.visit(this, arpMessage);
	}

}
