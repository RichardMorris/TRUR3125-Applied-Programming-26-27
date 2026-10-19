package org.singsurf.jns3.network_layer;

import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.hardware.AddressableDevice;

public class EthernetPacketSender implements PacketSender {
	String name;
	MACAddress sourceMac;
	ARPController arpController;
	FrameSender frameSender;

	FrameFactory frameFactory = FrameFactory.getInstance();
	PacketFragmentor packetFragmentor = new PacketFragmentor(140);
	
	public EthernetPacketSender(AddressableDevice device, 
			ARPController arpController, FrameSender frameSender) {
		this.name = device.getName();
		this.sourceMac = device.getMACAddress();
		this.arpController = arpController;
		this.frameSender = frameSender;
	}

	@Override
	public void sendPacket(IPPacket packet) {
		
		var destMac = arpController.getArpTable().get(packet.getDestinationAddress());
		if(destMac == null) {
			Jns3Logger.logError(name, "No ARP entry for " + packet.getDestinationAddress() + ". Cannot send packet.");
			return;
		}
		List<IPPacket> packets = packetFragmentor.getFragments(packet);
		for(IPPacket p : packets) {
			EthernetFrame frame = frameFactory.createEthernetFrame(
					destMac, sourceMac, p.toBytes());
			frameSender.sendFrame(frame);
		}
	}

}
