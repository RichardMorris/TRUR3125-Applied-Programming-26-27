package org.singsurf.jns3.data_link_layer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestEthernetFrame {
    @Test 
    public void testCreateEthernetFrame() {
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");

        byte[] payload = new byte[] {
            0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x45, 0x74, 0x68, 0x65, 0x72, 0x6E, 0x65, 0x74, 0x21
        };
        FrameFactory factory = new FrameFactory();
        EthernetFrame frame = factory.createEthernetFrame(destination, source, (short) payload.length, payload);
        assertEquals(destination, frame.getDestination());
        assertEquals(source, frame.getSource());
        assertEquals((short) payload.length, frame.getEtherType());
        assertArrayEquals(payload, frame.getPayload());
    }
}
