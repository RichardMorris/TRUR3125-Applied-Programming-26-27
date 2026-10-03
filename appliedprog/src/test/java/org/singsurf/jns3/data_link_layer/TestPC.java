package org.singsurf.jns3.data_link_layer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;

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
        MACAddress destination = pc.getMacAddress();
        MACAddress source = deviceFactory.createMACAddress();

        EthernetFrame frame = frameFactory.createEthernetFrame(destination, source, 
            "Hello, PC!");

        Jns3Logger.clearLog();
        pc.receiveFrame(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        assertEquals(1, logs.size());
        assertEquals("TestPC", logs.get(0).deviceName);
        assertEquals(Jns3Logger.LogLevel.RECEIVED, logs.get(0).level);
        assertEquals("Hello, PC!", logs.get(0).message);
    }

    @Test 
    public void receiveFrameWithWrongDestination() {
        PC pc = deviceFactory.createPC("TestPC");
        MACAddress destination = deviceFactory.createMACAddress();
        MACAddress source = pc.getMacAddress();

        EthernetFrame frame = frameFactory.createEthernetFrame(destination, source, 
            "Hello, PC!");

        Jns3Logger.clearLog();
        pc.receiveFrame(frame);
        List<LogItem> logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(1, logs.size());
        assertEquals("TestPC", logs.get(0).deviceName);
        assertEquals(Jns3Logger.LogLevel.IGNORED, logs.get(0).level);
        assertEquals("Hello, PC!", logs.get(0).message);
    }

}
