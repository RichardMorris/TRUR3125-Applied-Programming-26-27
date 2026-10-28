package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestEthernetFrame {
    @Test 
    public void testCreateEthernetFrame() {
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");

        byte[] payload = new byte[] {
            0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x45, 0x74, 0x68, 0x65, 0x72, 0x6E, 0x65, 0x74, 0x21
        };
        EthernetFrame frame = new EthernetFrame(destination, source, (short) payload.length, payload);
        assertEquals(destination, frame.getDestination());
        assertEquals(source, frame.getSource());
        assertEquals((short) payload.length, frame.getEtherType());
        assertArrayEquals(payload, frame.getContents());
    }

    @Test
    public void testByteToFrameConverter() {
    	var converter = new ByteToFrameConverter();
    	
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");
        byte[] payload = new byte[] {
            0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x45, 0x74, 0x68, 0x65, 0x72, 0x6E, 0x65, 0x74, 0x21
        };
        EthernetFrame frame = new EthernetFrame(destination, source, (short) payload.length, payload);
        byte[] frameBytes = converter.toBytes(frame);
        
        
        EthernetFrame parsedFrame = converter.fromBytes(frameBytes);
        assertEquals(destination, parsedFrame.getDestination());
        assertEquals(source, parsedFrame.getSource());
        assertEquals((short) payload.length, parsedFrame.getEtherType());
        assertArrayEquals(payload, parsedFrame.getContents());
        assertEquals(frame, parsedFrame);
    }

}
