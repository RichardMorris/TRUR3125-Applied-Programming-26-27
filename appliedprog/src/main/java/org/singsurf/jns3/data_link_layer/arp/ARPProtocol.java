package org.singsurf.jns3.data_link_layer.arp;

import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;

public class ARPProtocol implements LowerAdapter<EthernetFrame> {

	AddressableDevice device;
	ARPController arpController;

	public ARPProtocol(AddressableDevice device) {
		this.device = device;
		this.arpController = new ARPController(device);
	}

	@Override
	public List<ProtocolHandler<EthernetFrame>> getInboundHandlers() {
		return List.of(arpController);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<EthernetFrame> receiver) {
		
	}

	@Override
	public void setOutboundSender(ProtocolSender<EthernetFrame> sender) {
		arpController.setSender((FrameSender) sender);
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
		return arpController.lookup(ipAddress);
	}

}
