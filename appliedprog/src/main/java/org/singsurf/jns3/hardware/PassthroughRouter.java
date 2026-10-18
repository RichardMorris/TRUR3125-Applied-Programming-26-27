package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.ARPMessage;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.network_layer.icmp.PingRequestHandler;

/**
 * A PassthroughRouter is a simple router that forwards Ethernet frames between its two ports.
 * If the frame is an ARPMessage then forwarded to the other port,
 * hence the router is invisible to ARP. 
 * If the frame is an IPPacket then 
 * 
 *  otherwise it is dropped. This is a simple implementation of a router that does not perform any routing logic.
 */
public class PassthroughRouter extends MultiPortDevice implements AddressableDevice {
    List<FrameHandler> frameHandlers = new ArrayList<>();
    IPAddress address;
    FrameFactory frameFactory = FrameFactory.getInstance();
    MACAddress macAddress = DeviceFactory.getInstance().createMACAddress();
    ARPController arpController1;
    ARPController arpController2;
	private Sender sender1;
	private Sender sender2;
	private PingRequestHandler pingreq1;
	private PingRequestHandler pingreq2;
    
    class Sender implements FrameSender, PacketSender {
		int portNumber;
		MACAddress destMac;
		public Sender(int portNumber) {
			this.portNumber = portNumber;
		}
		@Override
		public void sendFrame(EthernetFrame frame) {
			PassthroughRouter.this.sendFrame(portNumber, frame);
		}
		@Override
		public void sendPacket(IPPacket packet) {
			var frame = frameFactory.createEthernetFrame(
					macAddress,destMac, packet.toBytes());
			sendFrame(frame);
		}
		public void setDestMac(MACAddress dest) {
			destMac = dest;
		}
	}
    static class Addressable implements AddressableDevice {
    	public Addressable(String name, IPAddress ipAddress, MACAddress macAddress) {
			super();
			this.name = name;
			this.macAddress = macAddress;
			this.ipAddress = ipAddress;
		}
		public String getName() {
			return name;
		}
		public MACAddress getMACAddress() {
			return macAddress;
		}
		public IPAddress getIPAddress() {
			return ipAddress;
		}
		String name;
    	MACAddress macAddress;
    	IPAddress ipAddress;
    	
    	
    }
    
    public PassthroughRouter(String name, IPAddress address) {
		super(name, 2);
		this.address = address;
	    sender1 = new Sender(0);
	    sender2 = new Sender(1);
		arpController1 = new ARPController(new Addressable(name,address,macAddress), 
	    		sender1);
	    arpController2 = new ARPController(new Addressable(name,address,macAddress), 
	    		sender2);
		pingreq1 = new PingRequestHandler(name,address, sender1);
		pingreq2 = new PingRequestHandler(name,address, sender2);

	}

	@Override
	public void receiveFrame(int portNumber, EthernetFrame frame) {
		if(!frame.isValidFrameCheck()) {
			Jns3Logger.logError(name, "Invalid frame check for frame "+frame);
			return;
		}
		if(ARPMessage.isARPMessage(frame)) {
			sendFrame(1 - portNumber, frame);
			if(portNumber == 0) {
				sender1.setDestMac(frame.getSource());
				arpController1.handleFrame(frame);
			} else {
				sender2.setDestMac(frame.getSource());
				arpController2.handleFrame(frame);
			}
			return;
		}
		var payload = frame.getPayload();
		if(!IPPacket.isIPPacket(payload)) {
			Jns3Logger.logError(name, "Non IP packet dropped "+frame);
			return;
		}
		var packet = IPPacket.fromBytes(payload);
		if(!packet.isValidChecksum()) {
			Jns3Logger.logError(name, "Invalid checksum for packet "+packet);
			return;
		}
		packet.decrementTTL();
		if(packet.getTimeToLive() > 0) {
			if(!packet.getDestinationAddress().equals(address) ) {
				sendFrame(1 - portNumber, frame);
				return;
			}
			if(!ICMPMessage.isICMPMessage(packet)) {
				Jns3Logger.logError(name, "Received non ICMPPacket directed here"+packet);
				return;
			}
		}				
		if(!ICMPMessage.isICMPMessage(packet)) {
			Jns3Logger.logInfo(name, "Dropped packet "+packet.toString());
			return;
		}
		var icmp = ICMPMessage.parsePacket(packet);
		if(icmp.isParemeterProblem()) {
			var errorPacket = new IPPacket(
				IPPacket.IMCP, 64,
				address, packet.getSourceAddress(), 
				icmp.getBytes());
			var errorFrame = frameFactory.createEthernetFrame(
				frame.getSource(),frame.getDestination(), errorPacket.toBytes());
			Jns3Logger.logInfo(name, "Parameter problem "+icmp);

			sendFrame(portNumber,errorFrame);
			return;
		} 

		if(icmp.isPingRequest()) {
			if(packet.getDestinationAddress().equals(address) 
					&& packet.getTimeToLive() > 0) {

				if(portNumber == 0) {
					sender1.setDestMac(frame.getSource());
					pingreq1.handle(packet, icmp);
				} else {
					sender2.setDestMac(frame.getSource());
					pingreq2.handle(packet, icmp);
				}
				return;
			}
			var errorMsg = ICMPMessage.createTTLExpired(packet);
			var errorPacket = new IPPacket(
				IPPacket.IMCP, 64,
				address, packet.getSourceAddress(), 
				errorMsg.getBytes());
			var errorFrame = frameFactory.createEthernetFrame(
				frame.getSource(),frame.getDestination(), errorPacket.toBytes());
			Jns3Logger.logInfo(name, "Expired ping "+icmp);

			sendFrame(portNumber,errorFrame);
		} else {
			Jns3Logger.logError(name, "Unknown ICMP message type "+icmp);
		}
	}

	public IPAddress getIPAddress() {
		return address;
	}

	@Override
	public MACAddress getMACAddress() {
		return macAddress;
	}

}
