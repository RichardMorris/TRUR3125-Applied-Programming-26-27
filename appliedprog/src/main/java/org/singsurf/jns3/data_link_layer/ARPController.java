package org.singsurf.jns3.data_link_layer;
import java.util.HashMap;
import java.util.Map;
import org.singsurf.jns3.Jns3Logger;

public class ARPController implements FrameHandler {
    FrameFactory frameFactory;
    public static MACAddress ARP_BROADCAST_MAC = new MACAddress(new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF});
    public static short ARP_ETHER_TYPE = 0x0806; // EtherType for ARP
    Map<IPAddress, MACAddress> arpTable = new HashMap<>();
    Map<MACAddress, IPAddress> reverseArpTable = new HashMap<>();

    Device device;

    public ARPController(Device device) {
        this.device = device;
        this.frameFactory = device.getFrameFactory();
    }
    
    @Override
    public boolean handleFrame(EthernetFrame frame) {
        if(frame.getEtherType() != ARP_ETHER_TYPE) return false;

        if(frame.getDestination().equals(ARP_BROADCAST_MAC)
            || frame.getDestination().equals(device.getMacAddress())) {

                Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.RECEIVED,  "Received ARP frame");
                var arpMessage = ARPMessage.fromFrame(frame);
                // Update ARP tables
                arpTable.put(arpMessage.getSenderIp(), arpMessage.getSenderMac());
                reverseArpTable.put(arpMessage.getSenderMac(), arpMessage.getSenderIp());

                if(arpMessage.isRequest()) {
                    // Respond to ARP request
                    var reply = new ARPMessage(
                        false, 
                        device.getMacAddress(), 
                        device.getIpAddress(), 
                        arpMessage.getSenderMac(), 
                        arpMessage.getSenderIp());
                    var replyFrame = frameFactory.createEthernetFrame(
                        arpMessage.getSenderMac(), 
                        device.getMacAddress(), 
                        ARP_ETHER_TYPE,
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
            device.getMacAddress(),
            device.getIpAddress());
        
        var garpFrame = frameFactory.createEthernetFrame(
            ARP_BROADCAST_MAC,
            device.getMacAddress(),
            ARP_ETHER_TYPE,
            garpMessage.toBytes());
        Jns3Logger.log(device.getName(), Jns3Logger.LogLevel.SENT,  "Sent GARP request frame");
        device.sendFrame(garpFrame);
    }

    public Map<IPAddress, MACAddress> getArpTable() {
        return arpTable;
    }

    public Map<MACAddress, IPAddress> getReverseArpTable() {
        return reverseArpTable;
    }
}