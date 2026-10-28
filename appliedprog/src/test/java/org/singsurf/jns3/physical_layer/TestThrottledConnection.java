package org.singsurf.jns3.physical_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;

public class TestThrottledConnection {
    private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc1.setEndPoint(new NICCard("PC1-EP",pc1));
        pc2 = deviceFactory.createPC("PC2");
        pc2.setEndPoint(new NICCard("PC2-EP",pc1));
        var connection = new ThrottledConnection(pc1.getEndPoint(), pc2.getEndPoint(),
        		ThrottledConnection.CAT3);
        
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test
    public void only_16_frames_get_through_per_tick() {
            Jns3Logger.clearLog();
            for(int i = 1; i <= 50; i++) {
				byte[] payload = ("Hello #"+i).getBytes();
				short etherType = (short) payload.length;
			var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
                pc1.sendFrame(frame);
            }
            var logs = Jns3Logger.getLogItems();
            assertEquals(16, logs.size()); 
            assertTrue(Jns3Logger.isErrorFree());
            
            Jns3Logger.clearLog();
            GlobalWallClock.getInstance().advanceTime();
            assertEquals(16, logs.size()); 

            Jns3Logger.clearLog();
            GlobalWallClock.getInstance().advanceTime();
            assertEquals(16, logs.size()); 

            Jns3Logger.clearLog();
            GlobalWallClock.getInstance().advanceTime();
            assertEquals(2, logs.size()); 

    }
}
