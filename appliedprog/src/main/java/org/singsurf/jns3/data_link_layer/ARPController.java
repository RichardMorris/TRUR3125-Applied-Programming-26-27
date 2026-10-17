package org.singsurf.jns3.data_link_layer;

import java.util.HashMap;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;

public class ARPController implements FrameHandler {
    FrameFactory frameFactory;
    Map<IPAddress, MACAddress> arpTable = new HashMap<>();
    Map<MACAddress, IPAddress> reverseArpTable = new HashMap<>();

    PC device;

    public ARPController(PC device) {
        this.device = device;
        this.frameFactory = device.getFrameFactory();
    }
    
    @Override
    public boolean handleFrame(EthernetFrame frame) {
        if(frame.getEtherType() != ARPMessage.ARP_ETHER_TYPE) return false;

        if(frame.getDestination().equals(MACAddress.BROADCAST_MAC)
            || frame.getDestination().equals(device.getMACAddress())) {

                Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  "Received ARP frame");
                var arpMessage = ARPMessage.fromFrame(frame);
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
                    var replyFrame = frameFactory.createEthernetFrame(
                        arpMessage.getSenderMac(), 
                        device.getMACAddress(), 
                        ARPMessage.ARP_ETHER_TYPE,
                        reply.toBytes());
                Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent ARP reply frame");
                    device.sendFrame(replyFrame);
                }
        } else {
            Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.IGNORED,  "Received ARP frame not for this device");
        }


        return true;
    }

    public void sendGARPRequest() {
        var garpMessage = ARPMessage.createGarpRequest(
            device.getMACAddress(),
            device.getIPAddress());
        
        var garpFrame = frameFactory.createEthernetFrame(
            MACAddress.BROADCAST_MAC,
            device.getMACAddress(),
            ARPMessage.ARP_ETHER_TYPE,
            garpMessage.toBytes());
        Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent GARP request frame");
        device.sendFrame(garpFrame);
    }

    public void sendArpRequest(IPAddress ip) {
        var request = new ARPMessage(
            true,
            device.getMACAddress(),
            device.getIPAddress(),
            MACAddress.ZERO_MAC,
            ip);
        var requestFrame = frameFactory.createEthernetFrame(
            MACAddress.BROADCAST_MAC,
            device.getMACAddress(),
            ARPMessage.ARP_ETHER_TYPE,
            request.toBytes());
        Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent ARP Probe request frame");
        device.sendFrame(requestFrame);
    }

    public Map<IPAddress, MACAddress> getArpTable() {
        return arpTable;
    }

    public Map<MACAddress, IPAddress> getReverseArpTable() {
        return reverseArpTable;
    }
}