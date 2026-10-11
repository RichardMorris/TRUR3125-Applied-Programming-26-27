package org.singsurf.jns3.physical_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;

public class TestLossyConnection {
    private PC pc1;
    private PC pc2;

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = deviceFactory.createPC("PC2");
        var connection = new LossyConnection(pc1.getEndPoint(), pc2.getEndPoint());
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test
    public void drop_every_10th_frame() {
            Jns3Logger.clearLog();
            for(int i = 1; i <= 24; i++) {
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello #"+i);
                pc1.sendFrame(frame);
            }
            var logs = Jns3Logger.getLogItems();
            //System.out.println(logs);
            assertEquals(logs.size(), 24 - 2); // Every 10th frame is dropped, so 2 frames should be missing    
    }
}
