package org.singsurf.jns3.data_link_layer.arp;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;

/**
 * Controls the Address Resolution Protocol that
 * allows PC's to request each each other MAC address
 * from from an IP address.
 * 
 * For each device hold a table of IP to MAC address mappings and vice versa.
 */
public class ARPForwarder implements FrameHandler {
    Map<IPAddress, MACAddress> arpTable = new HashMap<>();
    Map<MACAddress, IPAddress> reverseArpTable = new HashMap<>();
    ARPExtractor arpConverter = ARPExtractor.getInstance();
    AddressableDevice device;
    FrameSender sender;

    public ARPForwarder(AddressableDevice device, FrameSender sender) {
		this.device = device;
		this.sender = sender;
	}
    
    public ARPForwarder(AddressableDevice device) {
		this.device = device;
	}


    /**
     * Sends a Gratuitous ARP (GARP) request to announce the device's IP and MAC address to the network.
     */
    public void sendGARPRequest() {
        var garpMessage = ARPMessage.createGarpRequest(
            device.getMACAddress(),
            device.getIPAddress());
        
        var garpFrame = new EthernetFrame(MACAddress.BROADCAST_MAC, device.getMACAddress(), ARPMessage.ARP_ETHER_TYPE, garpMessage.getBytes());
        
        Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent GARP request frame");
        sender.send(garpFrame);
    }

    /**
	 * Sends an ARP request to resolve the MAC address for the given IP address.
	 * @param ip The IP address to resolve.
	 */
    public void sendArpRequest(IPAddress ip) {
        var request = new ARPMessage(
            true,
            device.getMACAddress(),
            device.getIPAddress(),
            MACAddress.ZERO_MAC,
            ip);
        var requestFrame = new EthernetFrame(MACAddress.BROADCAST_MAC, device.getMACAddress(), ARPMessage.ARP_ETHER_TYPE, request.getBytes());
        Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent ARP Probe request frame");
        sender.send(requestFrame);
    }

    public Map<IPAddress, MACAddress> getArpTable() {
        return arpTable;
    }

    public Map<MACAddress, IPAddress> getReverseArpTable() {
        return reverseArpTable;
    }

    /**
     * Allows manual entry into the table.
     * @param ip
     * @param macAddress
     * @return
     */
	public MACAddress put(IPAddress ip, MACAddress macAddress) {
		var existingMac = arpTable.put(ip, macAddress);
		reverseArpTable.put(macAddress, ip);
		return existingMac;
	}

	/**
	 * Looks up the MAC address for a given IP address in the ARP table.
	 * @param ipAddress The IP address to look up.
	 * @return The corresponding MAC address, or null if not found.
	 */
	public MACAddress lookup(IPAddress ipAddress) {
		return arpTable.get(ipAddress);
	}

	@Override
	public HandlerStatus handle(EthernetFrame frame) {
	        if(frame.getEtherType() != ARPMessage.ARP_ETHER_TYPE) return HandlerStatus.CONTINUE;

	        if(frame.getDestination().equals(MACAddress.BROADCAST_MAC)) {
	        	sender.send(frame); // Forward the ARP frame to all devices
		        return HandlerStatus.CONTINUE; // Continue processing the ARP frame
	        }
	        if(!frame.getDestination().equals(device.getMACAddress())) {
	        	// not for this device, forward
	        	sender.send(frame);
	        	return HandlerStatus.COMPLETED;
	        }
	        return HandlerStatus.CONTINUE;
	}

	public void setSender(FrameSender sender) {
		this.sender = sender;
	}
	
	
}