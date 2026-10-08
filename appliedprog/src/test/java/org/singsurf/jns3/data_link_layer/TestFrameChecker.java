package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.CorruptedConnection;
import org.singsurf.jns3.Jns3Logger;

public class TestFrameChecker {
        private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new CorruptedConnection(pc1.getEndPoint(), pc2.getEndPoint());
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test
    public void only_the_good_frames_are_received() {
        Jns3Logger.clearLog();
        for(int i=1;i<=20;++i) {
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello PC2 from PC1 "+i);
            pc1.sendFrame(frame);
        }

        var logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(20, logs.size());
        for(int i=1;i<=20;++i) {
            if(i%10!=0) {
                assertEquals(Jns3Logger.LogLevel.RECEIVED  , logs.get(i-1).level);
                assertEquals("PC2", logs.get(i-1).deviceName);
                assertEquals("Hello PC2 from PC1 "+i, logs.get(i-1).message);
            } else {
                assertEquals(Jns3Logger.LogLevel.ERROR  , logs.get(i-1).level);
                assertEquals("PC2", logs.get(i-1).deviceName);
                assertEquals("Detected invalid CRC in frame", 
                    logs.get(i-1).message);
            }
        }

    }
}
