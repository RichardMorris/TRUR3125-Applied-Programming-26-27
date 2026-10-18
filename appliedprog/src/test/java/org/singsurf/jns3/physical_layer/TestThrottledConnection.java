package org.singsurf.jns3.physical_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.NICCard;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.DefaultPacketReceiver;
import org.singsurf.jns3.network_layer.FragmentCombiner;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

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
        pc2.setEndPoint(new NICCard("PC2-EP",pc2));
        var connection = new ThrottledConnection(pc1.getEndPoint(), pc2.getEndPoint(),
        		ThrottledConnection.CAT3);
        
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }

    @Test
    public void only_16_frames_get_through_per_tick() {
            Jns3Logger.clearLog();
            for(int i = 1; i <= 50; i++) {
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello #"+i);
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
    
	@Test
	public void a_large_packet_can_be_transmission_over_a_throttled_network() {
		// Arrange

		var receiver = new DefaultPacketReceiver(pc1.getName());
		var combiner = new FragmentCombiner(pc1,receiver);
		pc1.clearFrameHandlers(); // Need set our own frame handlers to protect against later changes.
		pc1.addFrameHandler(pc1.getArpController());
		pc1.addFrameHandler(combiner);

		pc1.sendGARPRequest();
		
		StringBuilder sb = new StringBuilder();
		var itt = OnesComplementNumber.iterator(-255,255,1);
		while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString());
			sb.append('\n');
		}
		String dataString = sb.toString();
		
		// Act
		var packet = pc2.createIPPacket(
				IPPacket.TCP,
				pc1.getIPAddress(), 
				dataString);
		pc2.sendIPPacket(packet);
		
		GlobalWallClock.advanceBy(10);
		// Assert
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(pc2.getIPAddress(), receivedPacket.getSourceAddress());
        assertEquals(pc1.getIPAddress(), receivedPacket.getDestinationAddress());
        assertEquals(IPPacket.TCP, receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        System.out.println(receivedPacket.getDataAsString());
        assertTrue(Jns3Logger.isErrorFree());

	}

}
