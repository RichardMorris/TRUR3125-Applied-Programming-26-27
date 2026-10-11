package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameFactory;
import org.singsurf.jns3.data_link_layer.MACAddress;

public class TestPC {
    DeviceFactory deviceFactory = new DeviceFactory();
    FrameFactory frameFactory = new FrameFactory();
    @Test 
    public void createPC() {
        PC pc = deviceFactory.createPC("TestPC");
        assertNotNull(pc);
    }

    @Test 
    public void receiveFrame() {
        PC pc = deviceFactory.createPC("TestPC");
        MACAddress destination = pc.getMACAddress();
        MACAddress source = deviceFactory.createMACAddress();

        EthernetFrame frame = frameFactory.createEthernetFrame(destination, source, 
            "Hello, PC!");

        Jns3Logger.clearLog();
        pc.receiveFrame(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        assertEquals(1, logs.size());
        
        assertEquals(new Jns3Logger.LogItem("TestPC", 
        		Jns3Logger.LogLevel.RECEIVED,
        		"Hello, PC!"), 
        	logs.get(0));
    }

    @Test 
    public void receiveFrameWithWrongDestination() {
        PC pc = deviceFactory.createPC("TestPC");
        MACAddress destination = deviceFactory.createMACAddress();
        MACAddress source = pc.getMACAddress();

        EthernetFrame frame = frameFactory.createEthernetFrame(destination, source, 
            "Hello, PC!");

        Jns3Logger.clearLog();
        pc.receiveFrame(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(1, logs.size());
        
        assertEquals(new Jns3Logger.LogItem("TestPC", 
        		Jns3Logger.LogLevel.IGNORED,
        		"Hello, PC!"), 
        	logs.get(0));
    }

}
