package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.SimpleConnection;

public class TestAPRInitisation {

    private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test 
    public void testConnection() {
        Jns3Logger.clearLog();
		byte[] payload = "Hello PC2 from PC1".getBytes();
		short etherType = (short) payload.length;
        var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
        pc1.getFrameSender().send(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(1, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC2", Jns3Logger.LogLevel.RECEIVED,"Hello PC2 from PC1"), logs.get(0));
    }

    @Test
    public void testGRAP() {
        Jns3Logger.clearLog();
        var arpController = pc1.getArpController();
        assertEquals(1, arpController.getArpTable().size());
        assertEquals(1, arpController.getReverseArpTable().size());
        arpController.sendGARPRequest();

        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(4, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC1", 
        		Jns3Logger.LogLevel.SENT,"Sent GARP request frame"), logs.get(0));

        assertEquals(new Jns3Logger.LogItem("PC2", 
        		Jns3Logger.LogLevel.RECEIVED,"Received ARP frame"), logs.get(1));

        assertEquals(new Jns3Logger.LogItem("PC2", 
        		Jns3Logger.LogLevel.SENT,"Sent ARP reply frame"), logs.get(2));

        assertEquals(new Jns3Logger.LogItem("PC1", 
        		Jns3Logger.LogLevel.RECEIVED,"Received ARP frame"), logs.get(3));

        assertTrue(arpController.getArpTable().containsKey(pc2.getIPAddress()));
        assertTrue(arpController.getReverseArpTable().containsKey(pc2.getMACAddress())); 
        assertEquals(pc2.getMACAddress(), arpController.getArpTable().get(pc2.getIPAddress()));
        assertEquals(pc2.getIPAddress(), arpController.getReverseArpTable().get(pc2.getMACAddress()));

        assertTrue(pc2.getArpController().getArpTable().containsKey(pc1.getIPAddress()));
        assertTrue(pc2.getArpController().getReverseArpTable().containsKey(pc1.getMACAddress()));
        assertEquals(pc1.getMACAddress(), pc2.getArpController().getArpTable().get(pc1.getIPAddress()));
        assertEquals(pc1.getIPAddress(), pc2.getArpController().getReverseArpTable().get(pc1.getMACAddress()));
    }

    @Test
    public void testMessageToAnIP() {
        var arpController = pc1.getArpController();
        pc1.sendGARPRequest();

        Jns3Logger.clearLog();

        for(var entry : arpController.getArpTable().entrySet()) {
            var ipAddress = entry.getKey();
            if(ipAddress.equals(pc1.getMulticastAddress())) {
				continue; // Skip multicast entry
			}
            var macAddress = entry.getValue();
			byte[] payload = ("Hello " + ipAddress).getBytes();
			short etherType = (short) payload.length;
            var frame = new EthernetFrame(macAddress, pc1.getMACAddress(), etherType, payload);
            pc1.getFrameSender().send(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC2", 
        		Jns3Logger.LogLevel.RECEIVED,
        		"Hello "+ pc2.getIPAddress()), 
        	logs.get(0));
    }

    @Test
    public void testReverseMessageToAnIP() {
        var arpController = pc2.getArpController();
        arpController.sendGARPRequest();

        Jns3Logger.clearLog();
        
        for(var entry : arpController.getArpTable().entrySet()) {
            var ipAddress = entry.getKey();
            if(ipAddress.equals(pc1.getMulticastAddress())) {
				continue; // Skip multicast entry
			}
            var macAddress = entry.getValue();
			byte[] payload = ("Hello " + ipAddress).getBytes();
			short etherType = (short) payload.length;
            var frame = new EthernetFrame(macAddress, pc2.getMACAddress(), etherType, payload);
            pc2.getFrameSender().send(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC1", 
        		Jns3Logger.LogLevel.RECEIVED,
        		"Hello "+ pc1.getIPAddress()), 
        	logs.get(0));
    }

}
