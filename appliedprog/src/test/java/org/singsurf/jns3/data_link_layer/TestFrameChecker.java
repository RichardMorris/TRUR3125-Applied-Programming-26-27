package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.CorruptedConnection;

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
    public void only_the_good_frames_get_through() {
        Jns3Logger.clearLog();
        for(int i=1;i<=20;++i) {
			byte[] payload = ("Hello PC2 from PC1 "+i).getBytes();
			short etherType = (short) payload.length;
			var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
            pc1.getFrameSender().send(frame);
        }

        var logs = Jns3Logger.getLogItems();
        System.out.println(logs);
        assertEquals(20, logs.size());
        for(int i=1;i<=20;++i) {
            if(i%10!=0) {
                assertEquals(new Jns3Logger.LogItem("PC2", 
                		Jns3Logger.LogLevel.RECEIVED,
                		"Hello PC2 from PC1 "+i), 
                	logs.get(i-1));
            } else {
                assertEquals(new Jns3Logger.LogItem("PC2", 
                		Jns3Logger.LogLevel.ERROR,
                		"Invalid message: Detected invalid CRC in frame"), 
                	logs.get(i-1));
            }
        }

    }
}
