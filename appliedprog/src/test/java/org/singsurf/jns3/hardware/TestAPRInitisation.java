package org.singsurf.jns3.hardware;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.singsurf.jns3.transport_layer.SimpleConnection;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;

public class TestAPRInitisation {

    private Device pc1;
    private Device pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new SimpleConnection(pc1, pc2);
        ((PC) pc1).setConnection(connection);
        ((PC) pc2).setConnection(connection.getReverseConnection());
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test 
    public void testConnection() {
        Jns3Logger.clearLog();
        var frame = pc1.createEthernetFrame(
            pc2.getMacAddress(), 
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

        assertTrue(arpController.getArpTable().containsKey(pc2.getIpAddress()));
        assertTrue(arpController.getReverseArpTable().containsKey(pc2.getMacAddress())); 
        assertEquals(pc2.getMacAddress(), arpController.getArpTable().get(pc2.getIpAddress()));
        assertEquals(pc2.getIpAddress(), arpController.getReverseArpTable().get(pc2.getMacAddress()));

        assertTrue(pc2.getArpController().getArpTable().containsKey(pc1.getIpAddress()));
        assertTrue(pc2.getArpController().getReverseArpTable().containsKey(pc1.getMacAddress()));
        assertEquals(pc1.getMacAddress(), pc2.getArpController().getArpTable().get(pc1.getIpAddress()));
        assertEquals(pc1.getIpAddress(), pc2.getArpController().getReverseArpTable().get(pc1.getMacAddress()));
    }

    @Test
    public void testMessageToAnIP() {
        var arpController = pc1.getArpController();
        arpController.sendGARPRequest();

        Jns3Logger.clearLog();

        for(var entry : arpController.getArpTable().entrySet()) {
            var ipAddress = entry.getKey();
            var macAddress = entry.getValue();
            var frameFactory = pc1 .getFrameFactory();
            var frame = frameFactory.createEthernetFrame(
                macAddress, 
                pc1.getMacAddress(), 
                "Hello " + ipAddress);
            pc1.sendFrame(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(0).level);
        assertEquals("PC2", logs.get(0).deviceName);
        assertEquals("Hello "+ pc2.getIpAddress(), logs.get(0).message);
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
                pc2.getMacAddress(), 
                "Hello " + ipAddress);
            pc2.sendFrame(frame);
            break;
        }
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);   
        assertEquals(1, logs.size());
        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(0).level);
        assertEquals("PC1", logs.get(0).deviceName);
        assertEquals("Hello "+ pc1.getIpAddress(), logs.get(0).message);
    }

}
