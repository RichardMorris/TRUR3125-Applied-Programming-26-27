package org.singsurf.jns3.data_link_layer.arp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
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
        pc1.getEthernetProtocol().getFrameSender().send(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(1, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC2", Jns3Logger.LogLevel.RECEIVED,"Hello PC2 from PC1"), logs.get(0));
    }

    @Test
    public void testGRAP() {
        Jns3Logger.clearLog();
        var arpController = pc1.getArpProtocol().getArpController();
        arpController.sendGARPRequest();

        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(Jns3Logger.asString());
        assertEquals(4, logs.size());
        assertEquals(new Jns3Logger.LogItem("PC1-ARP", 
        		Jns3Logger.LogLevel.SENT,"Sent GARP request frame"), logs.get(0));

        assertEquals(new Jns3Logger.LogItem("PC2-ARP", 
        		Jns3Logger.LogLevel.RECEIVED,"Received ARP frame"), logs.get(1));

        assertEquals(new Jns3Logger.LogItem("PC2-ARP", 
        		Jns3Logger.LogLevel.SENT,"Sent ARP reply frame"), logs.get(2));

        assertEquals(new Jns3Logger.LogItem("PC1-ARP", 
        		Jns3Logger.LogLevel.RECEIVED,"Received ARP frame"), logs.get(3));

        assertEquals(pc2.getMACAddress(), arpController.lookup(pc2.getIPAddress()));

        assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));
    }


}
