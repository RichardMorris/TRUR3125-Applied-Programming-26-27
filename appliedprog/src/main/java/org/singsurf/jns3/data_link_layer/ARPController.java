package org.singsurf.jns3.data_link_layer;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;

/**
 * Controls the Address Resolution Protocol that
 * allows PC's to request each each other MAC address
 * from from an IP address.
 * 
 * For each device hold a table of IP to MAC address mappings and vice versa.
 */
public class ARPController implements FrameHandler {
    Map<IPAddress, MACAddress> arpTable = new HashMap<>();
    Map<MACAddress, IPAddress> reverseArpTable = new HashMap<>();
    ARPExtractor arpConverter = ARPExtractor.getInstance();
    AddressableDevice device;
    FrameSender sender;

    public ARPController(AddressableDevice device, FrameSender sender) {
		this.device = device;
		this.sender = sender;
	}
    
    private boolean handleFrame(EthernetFrame frame) {
        if(frame.getEtherType() != ARPMessage.ARP_ETHER_TYPE) return false;

        if(frame.getDestination().equals(MACAddress.BROADCAST_MAC)
            || frame.getDestination().equals(device.getMACAddress())) {

                Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  "Received ARP frame");
                var arpMessage = arpConverter.extract(frame);
                // Update ARP tables
                arpTable.put(arpMessage.getSenderIp(), arpMessage.getSenderMac());
                reverseArpTable.put(arpMessage.getSenderMac(), arpMessage.getSenderIp());

                if(arpMessage.isRequest()) {
                    // Respond to ARP request
                    var reply = new ARPMessage(
                        false, 
                        device.getMACAddress(), 
                        device.getIPAddress(), 
                        arpMessage.getSenderMac(), 
                        arpMessage.getSenderIp());
                    var replyFrame = new EthernetFrame(arpMessage.getSenderMac(), device.getMACAddress(), ARPMessage.ARP_ETHER_TYPE, reply.getBytes());
                Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent ARP reply frame");
                    sender.send(replyFrame);
                }
        } else {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.IGNORED,  "Received ARP frame not for this device");
        }


        return true;
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
	public HandlerStatus handle(EthernetFrame type) {
		return handleFrame(type) ? ProtocolHandler.HANDLED : ProtocolHandler.IGNORED;
	}
}