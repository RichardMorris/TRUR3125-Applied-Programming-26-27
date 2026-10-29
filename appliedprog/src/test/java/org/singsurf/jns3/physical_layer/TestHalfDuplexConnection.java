package org.singsurf.jns3.physical_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.singsurf.jns3.Jns3Logger.LogLevel.RECEIVED;
import static org.singsurf.jns3.Jns3Logger.LogLevel.SENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;

public class TestHalfDuplexConnection {
    private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new HalfDuplexConnection(pc1.getEndPoint(), pc2.getEndPoint(),
        		ThrottledConnection.CAT3);
        
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test
    public void only_transmit_frames_in_one_direction_per_tick() {
        Jns3Logger.clearLog();
		byte[] payload = "Forward 1".getBytes();
		short etherType = (short) payload.length;
        var frame1 = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
        pc1.getEthernetProtocol().getFrameSender().send(frame1);
		byte[] payload1 = "Reversed 1".getBytes();
		short etherType1 = (short) payload1.length;
     
        var frame2 = new EthernetFrame(pc1.getMACAddress(), pc2.getMACAddress(), etherType1, payload1);  
        pc2.getEthernetProtocol().getFrameSender().send(frame2);
        
        var logs = Jns3Logger.getLogItems();
        
        assertEquals(1, logs.size());
        assertEquals(new LogItem("PC2",RECEIVED,"Forward 1"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(1, logs.size()); 
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 1"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());

        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(0, logs.size()); 

        Jns3Logger.clearLog();
		byte[] payload2 = "Reversed 2".getBytes();
		short etherType2 = (short) payload2.length;
        var frame3 = new EthernetFrame(pc1.getMACAddress(), pc2.getMACAddress(), etherType2, payload2);
		byte[] payload3 = "Forward 2".getBytes();
		short etherType3 = (short) payload3.length; 
        var frame4 = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType3, payload3);
		byte[] payload4 = "Reversed 3".getBytes();
		short etherType4 = (short) payload4.length;
        var frame5 = new EthernetFrame(pc1.getMACAddress(), pc2.getMACAddress(), etherType4, payload4);
        
        pc2.getEthernetProtocol().getFrameSender().send(frame3);
        pc1.getEthernetProtocol().getFrameSender().send(frame4);
        pc2.getEthernetProtocol().getFrameSender().send(frame5);
        
        assertEquals(2, logs.size()); 
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 2"), logs.get(0));
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 3"), logs.get(1));
        assertTrue(Jns3Logger.isErrorFree());
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(1, logs.size()); 
        assertEquals(new LogItem("PC2",RECEIVED,"Forward 2"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());
    }

    @Test
    public void two_ticks_for_arp_negotiation() {
        Jns3Logger.clearLog();
        pc1.getArpProtocol().getArpController().sendGARPRequest();
        System.out.println(Jns3Logger.asString());
        assertEquals(3, Jns3Logger.size());
        var logs = Jns3Logger.getLogItems();
        assertEquals(new LogItem("PC1",SENT,"Sent GARP request frame"), logs.get(0));
        assertEquals(new LogItem("PC2",RECEIVED,"Received ARP frame"), logs.get(1));
        assertEquals(new LogItem("PC2",SENT,"Sent ARP reply frame"), logs.get(2));
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(1, Jns3Logger.size());
        logs = Jns3Logger.getLogItems();
        assertEquals(new LogItem("PC1",RECEIVED,"Received ARP frame"), logs.get(0));
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        var mac1 = pc2.getArpProtocol().getArpController().lookup(pc1.getIPAddress());
        var mac2 = pc1.getArpProtocol().getArpController().lookup(pc2.getIPAddress());
		byte[] payload = "Forward 1".getBytes();
		short etherType = (short) payload.length;
        
        var frame1 = new EthernetFrame(mac2, pc1.getMACAddress(), etherType, payload);
        pc1.getEthernetProtocol().getFrameSender().send(frame1);
		byte[] payload1 = "Reversed 1".getBytes();
		short etherType1 = (short) payload1.length;
     
        var frame2 = new EthernetFrame(mac1, pc2.getMACAddress(), etherType1, payload1);  
        pc2.getEthernetProtocol().getFrameSender().send(frame2);
        
        logs = Jns3Logger.getLogItems();
        
        assertEquals(1, logs.size());
        assertEquals(new LogItem("PC2",RECEIVED,"Forward 1"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(1, logs.size()); 
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 1"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());

        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(0, logs.size()); 

        Jns3Logger.clearLog();
		byte[] payload2 = "Reversed 2".getBytes();
		short etherType2 = (short) payload2.length;
        var frame3 = new EthernetFrame(mac1, pc2.getMACAddress(), etherType2, payload2);
		byte[] payload3 = "Forward 2".getBytes();
		short etherType3 = (short) payload3.length; 
        var frame4 = new EthernetFrame(mac2, pc1.getMACAddress(), etherType3, payload3);
		byte[] payload4 = "Reversed 3".getBytes();
		short etherType4 = (short) payload4.length;
        var frame5 = new EthernetFrame(mac1, pc2.getMACAddress(), etherType4, payload4);
        
        pc2.getEthernetProtocol().getFrameSender().send(frame3);
        pc1.getEthernetProtocol().getFrameSender().send(frame4);
        pc2.getEthernetProtocol().getFrameSender().send(frame5);
        
        assertEquals(2, logs.size()); 
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 2"), logs.get(0));
        assertEquals(new LogItem("PC1",RECEIVED,"Reversed 3"), logs.get(1));
        assertTrue(Jns3Logger.isErrorFree());
        
        Jns3Logger.clearLog();
        GlobalWallClock.getInstance().advanceTime();
        assertEquals(1, logs.size()); 
        assertEquals(new LogItem("PC2",RECEIVED,"Forward 2"), logs.get(0));
        assertTrue(Jns3Logger.isErrorFree());
    }

}
