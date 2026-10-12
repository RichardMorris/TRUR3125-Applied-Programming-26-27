package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPacketFragmentor {
	PacketFragmentor fragmentor = new PacketFragmentor(100);
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void small_packets_unchanged() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, destination, data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(1, fragments.size());
        assertEquals(packet, fragments.get(0));
        assertTrue(fragments.get(0).isValidChecksum());
        assertFalse(fragments.get(0).isMoreFragments());
        assertTrue(fragments.get(0).getTotalLength() <= 100);
	}

	
	@Test
	void split_packets_in_two() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        
        byte[] data = new byte[150];
        for(int i=0;i<data.length;i++) data[i] = (byte) i;
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, destination, data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(2, fragments.size());
        var frag1 = fragments.get(0);
        var frag2 = fragments.get(1);
        assertTrue(frag1.isMoreFragments());
        assertFalse(frag2.isMoreFragments());
        assertTrue(frag1.getTotalLength() <= 100);
        assertTrue(frag2.getTotalLength() <= 100);
        assertEquals(0,frag1.getDataLength() %8 );
        assertEquals(frag1.getSourceAddress(), packet.getSourceAddress());
        assertEquals(frag1.getDestinationAddress(), packet.getDestinationAddress());
        assertEquals(frag2.getSourceAddress(), packet.getSourceAddress());
        assertEquals(frag2.getDestinationAddress(), packet.getDestinationAddress());
        assertEquals(1, frag1.getIdentification());
        assertEquals(1, frag2.getIdentification());
        assertTrue(frag1.isValidChecksum());
        assertTrue(frag2.isValidChecksum());
        assertEquals(frag1.getProtocol(), packet.getProtocol());
        assertEquals(frag2.getProtocol(), packet.getProtocol());
        
        byte [] combinedData = new byte[frag1.getData().length + frag2.getData().length];
        System.arraycopy(frag1.getData(), 0, combinedData, 0, frag1.getData().length);
        System.arraycopy(frag2.getData(), 0, combinedData, frag1.getData().length, frag2.getData().length);	
        assertArrayEquals(packet.data, combinedData);
	}

	@Test
	void split_packets_in_three() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        
        byte[] data = new byte[200];
        for(int i=0;i<data.length;i++) data[i] = (byte) (i & 0xFF);
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, destination, data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(3, fragments.size());
        var frag1 = fragments.get(0);
        var frag2 = fragments.get(1);
        var frag3 = fragments.get(2);
        assertTrue(frag1.isMoreFragments());
        assertTrue(frag2.isMoreFragments());
        assertFalse(frag3.isMoreFragments());
        assertTrue(frag1.getTotalLength() <= 100);
        assertTrue(frag2.getTotalLength() <= 100);
        assertTrue(frag3.getTotalLength() <= 100);
        assertEquals(0,frag1.getDataLength() %8 );
        assertEquals(0,frag2.getDataLength() %8 );
        assertEquals(frag1.getSourceAddress(), packet.getSourceAddress());
        assertEquals(frag1.getDestinationAddress(), packet.getDestinationAddress());
        assertEquals(frag2.getSourceAddress(), packet.getSourceAddress());
        assertEquals(frag2.getDestinationAddress(), packet.getDestinationAddress());
        assertEquals(frag3.getSourceAddress(), packet.getSourceAddress());
        assertEquals(frag3.getDestinationAddress(), packet.getDestinationAddress());
        assertEquals(1, frag1.getIdentification());
        assertEquals(1, frag2.getIdentification());
        assertEquals(1, frag3.getIdentification());
        assertTrue(frag1.isValidChecksum());
        assertTrue(frag2.isValidChecksum());
        assertTrue(frag3.isValidChecksum());
        
        byte [] combinedData = new byte[frag1.getDataLength() + frag2.getDataLength() + frag3.getDataLength()];
        System.arraycopy(frag1.getData(), 0, combinedData, 0, frag1.getData().length);
        System.arraycopy(frag2.getData(), 0, combinedData, 
        		frag1.getData().length, frag2.getData().length);	
        System.arraycopy(frag3.getData(), 0, combinedData, 
        		frag1.getData().length + frag2.getData().length, frag3.getData().length);
        assertArrayEquals(packet.data, combinedData);
	}

}
