package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestFrameFactory {
    FrameFactory factory = new FrameFactory();
    
    /**
     * A Factory method to create multiple Ethernet frames from a message string.
     * @param destination the destination MAC address
     * @param source the source MAC address
     * @param message the message to be converted into the payload
     * @return the list of created Ethernet frames
     */
    public List<EthernetFrame> createEthernetFrames(
        MACAddress destination,
        MACAddress source,
        String message) {

        byte[] payload = message.getBytes();
        List<EthernetFrame> frames = new ArrayList<>();
        int pos = 0;
        while(pos < payload.length) {
            int chunkSize = Math.min(1500, payload.length - pos);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(payload, pos, chunk, 0, chunkSize);
            frames.add(new EthernetFrame(destination, source, (short) chunkSize, chunk));
            pos += chunkSize;
        }
        return frames;
    }


    @Test
    public void create_from_string() {
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");
        String message = "Hello, Ethernet!";
        EthernetFrame frame = factory.createEthernetFrame(destination, source, message);
        assertEquals(destination, frame.getDestination());
        assertEquals(source, frame.getSource());
        assertEquals((short) message.length(), frame.getEtherType());
        assertArrayEquals(message.getBytes(), frame.getContents());
        assertEquals(message, new String(frame.getContents()));
    }

    @Test
    public void create_from_utf8_string() {
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");
        String message = "π r²";
        EthernetFrame frame = factory.createEthernetFrame(destination, source, message);
        assertEquals(destination, frame.getDestination());
        assertEquals(source, frame.getSource());
        assertEquals(6, frame.getEtherType());
        assertArrayEquals(message.getBytes(), frame.getContents());
        assertEquals(message, new String(frame.getContents()));
    }

    @Test
    public void create_from_long_string() {
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String message = sb.toString();
        var frames = createEthernetFrames(destination, source, message);
        assertEquals(2,frames.size());
        EthernetFrame frame1 = frames.get(0);
        EthernetFrame frame2 = frames.get(1);

        assertEquals(destination, frame1.getDestination());
        assertEquals(source, frame1.getSource());
        assertEquals((short) 1500, frame1.getEtherType());
        assertArrayEquals(message.substring(0, 1500).getBytes(), frame1.getContents());

        assertEquals(destination, frame2.getDestination());
        assertEquals(source, frame2.getSource());
        assertEquals((short) (message.length() - 1500), frame2.getEtherType());
        assertArrayEquals(message.substring(1500).getBytes(), frame2.getContents());
		StringBuilder sb1 = new StringBuilder();
		for (EthernetFrame frame : frames) {
		    sb1.append(new String(frame.getContents()));
		}
        String res = sb1.toString();
        assertEquals(message, res);
        System.out.println(res);
    }
}
