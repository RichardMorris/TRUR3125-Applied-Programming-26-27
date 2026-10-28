package org.singsurf.jns3.hardware;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.ARPController;
import org.singsurf.jns3.data_link_layer.ARPMessage;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPPacketExtractor;
import org.singsurf.jns3.network_layer.PacketSender;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.network_layer.icmp.ICMPPacketExtractor;
import org.singsurf.jns3.network_layer.icmp.PingRequestHandler;

/**
 * A PassthroughRouter is a simple router that forwards Ethernet frames between its two ports.
 * If the frame is an ARPMessage then forwarded to the other port,
 * hence the router is invisible to ARP. 
 * If the frame is an IPPacket then the TTL is decremented.
 * If the TTL is zero then and the packet is an IMCP then an ICMP error is generated. 
 * This is a simple implementation of a router that does not perform any routing logic.
 * 
 * See <a href="../../../../../../Week10.md">Week 10</a>.
 */
public class PassthroughRouter extends MultiPortDevice implements AddressableDevice {
    IPAddress ipAddress;
    MACAddress macAddress = DeviceFactory.getInstance().createMACAddress();
    ARPController arpController;
	FSender fSender;
	PSender pSender;
	PingRequestHandler pingreq;
    
    class FSender implements FrameSender {
		int portNumber;
		MACAddress destMac;

		FSender() { }
		
		@Override
		public void send(EthernetFrame frame) {
			PassthroughRouter.this.sendFrame(portNumber, frame);
		}
		
		void setDestPortMac(int port,MACAddress dest) {
			portNumber = port;
			destMac = dest;
		}
	}

    class PSender implements PacketSender {
		int portNumber;
		MACAddress destMac;

		PSender() { }
				
		@Override
		public void send(IPPacket packet) {
			byte[] payload = packet.getBytes();
			var frame = new EthernetFrame(macAddress, destMac, (short) payload.length, payload);
			fSender.send(frame);
		}
		
		void setDestPortMac(int port,MACAddress dest) {
			portNumber = port;
			destMac = dest;
		}
	}

    public PassthroughRouter(String name, IPAddress address) {
		super(name, 2);
		this.ipAddress = address;
	    fSender = new FSender();
	    pSender = new PSender();
	    var addr1 = AddressableDevice.createAddressableDevice(name, macAddress, address);
		arpController = new ARPController(addr1, fSender);
		pingreq = new PingRequestHandler(addr1, pSender);

	}

	@Override
	public void receiveFrame(int portNumber, EthernetFrame frame) {
		if(!frame.isValid()) {
			Jns3Logger.logError(name, "Invalid frame check for frame "+frame);
			return;
		}
		if(ARPMessage.isARPMessage(frame)) {
			sendFrame(1 - portNumber, frame);
			fSender.setDestPortMac(portNumber,frame.getSource());
			arpController.handle(frame);
			return;
		}
		if(!IPPacketExtractor.getInstance().isExtractable(frame)) {
			Jns3Logger.logError(name, "Non IP packet dropped "+frame);
			return;
		}
		var packet = IPPacketExtractor.getInstance().extract(frame);
		if(!packet.isValid()) {
			Jns3Logger.logError(name, "Invalid checksum for packet "+packet);
			return;
		}
		packet.decrementTTL();
		if(packet.getTimeToLive() > 0) {
			if(!packet.getDestinationAddress().equals(ipAddress) ) {
				sendFrame(1 - portNumber, frame);
				return;
			}
			if(!ICMPPacketExtractor.getInstance().isExtractable(packet)) {
				Jns3Logger.logError(name, "Received non ICMPPacket directed here"+packet);
				return;
			}
		}				
		if(!ICMPPacketExtractor.getInstance().isExtractable(packet)) {
			Jns3Logger.logInfo(name, "Dropped packet "+packet.toString());
			return;
		}
		var icmp = ICMPPacketExtractor.getInstance().extract(packet);
		if(icmp.isParemeterProblem()) {
			var errorPacket = new IPPacket(
				IPPacket.IMCP, 64,
				ipAddress, packet.getSourceAddress(), 
				icmp.getBytes());
			byte[] payload1 = errorPacket.getBytes();
			var errorFrame = new EthernetFrame(frame.getSource(), frame.getDestination(), (short) payload1.length, payload1);
			Jns3Logger.logInfo(name, "Parameter problem "+icmp);

			sendFrame(portNumber,errorFrame);
			return;
		} 

		if(icmp.isPingRequest()) {
			if(packet.getDestinationAddress().equals(ipAddress) 
					&& packet.getTimeToLive() > 0) {

				pSender.setDestPortMac(portNumber,frame.getSource());
				pingreq.handle(packet, icmp);
				return;
			}
			var errorMsg = ICMPMessage.createTTLExpired(packet);
			var errorPacket = new IPPacket(
				IPPacket.IMCP, 64,
				ipAddress, packet.getSourceAddress(), 
				errorMsg.getBytes());
			byte[] payload1 = errorPacket.getBytes();
			var errorFrame = new EthernetFrame(frame.getSource(), frame.getDestination(), (short) payload1.length, payload1);
			Jns3Logger.logInfo(name, "Expired ping "+icmp);

			sendFrame(portNumber,errorFrame);
		} else {
			Jns3Logger.logError(name, "Unknown ICMP message type "+icmp);
		}
	}

	public IPAddress getIPAddress() {
		return ipAddress;
	}

	@Override
	public MACAddress getMACAddress() {
		return macAddress;
	}

	@Override
	public IPAddress getMulticastAddress() {
		return null;
	}

}
