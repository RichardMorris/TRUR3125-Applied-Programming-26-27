package org.singsurf.jns3.hardware;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.singsurf.jns3.physical_layer.SimpleConnection;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;

public class TestAPRInitisation {

    private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new SimpleConnection(pc1, pc2);
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test 
    public void testConnection() {
        Jns3Logger.clearLog();
        var frame = pc1.createEthernetFrame(
            pc2.getMACAddress(), 
            "Hello PC2 from PC1");
        pc1.sendFrame(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(1, logs.size());
        assertEquals(Jns3Logger.LogLevel.RECEIVED  , logs.get(0).level);
        assertEquals("PC2", logs.get(0).deviceName);
        assertEquals("Hello PC2 from PC1", logs.get(0).message);
    }

    @Test
    public void testGRAP() {
        Jns3Logger.clearLog();
        var arpController = pc1.getArpController();
        assertEquals(0, arpController.getArpTable().size());
        assertEquals(0, arpController.getReverseArpTable().size());
        arpController.sendGARPRequest();

        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(4, logs.size());
        assertEquals(Jns3Logger.LogLevel.SENT, logs.get(0).level);
        assertEquals("PC1", logs.get(0).deviceName);
        assertEquals("Sent GARP request frame", logs.get(0).message);   

        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(1).level);
        assertEquals("PC2", logs.get(1).deviceName);
        assertEquals("Received ARP frame", logs.get(1).message);    

        assertEquals(Jns3Logger.LogLevel.SENT, logs.get(2).level);
        assertEquals("PC2", logs.get(2).deviceName);
        assertEquals("Sent ARP reply frame", logs.get(2).message);

        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(3).level);
        assertEquals("PC1", logs.get(3).deviceName);
        assertEquals("Received ARP frame", logs.get(3).message);        

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
            var macAddress = entry.getValue();
            var frameFactory = pc1.getFrameFactory();
            var frame = frameFactory.createEthernetFrame(
                macAddress, 
                pc1.getMACAddress(), 
                "Hello " + ipAddress);
            pc1.sendFrame(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(0).level);
        assertEquals("PC2", logs.get(0).deviceName);
        assertEquals("Hello "+ pc2.getIPAddress(), logs.get(0).message);
    }

    @Test
    public void testReverseMessageToAnIP() {
        var arpController = pc2.getArpController();
        arpController.sendGARPRequest();

        Jns3Logger.clearLog();
        
        for(var entry : arpController.getArpTable().entrySet()) {
            var ipAddress = entry.getKey();
            var macAddress = entry.getValue();
            var frameFactory = pc2.getFrameFactory();
            var frame = frameFactory.createEthernetFrame(
                macAddress, 
                pc2.getMACAddress(), 
                "Hello " + ipAddress);
            pc2.sendFrame(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(0).level);
        assertEquals("PC1", logs.get(0).deviceName);
        assertEquals("Hello "+ pc1.getIPAddress(), logs.get(0).message);
    }

}
