package org.singsurf.jns3.network_layer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
public class TestIPPacket {

    @Test
    public void test_creation() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, destination, data); 
        assertEquals(4, packet.getVersion());
        assertEquals(5, packet.getHeaderLength());
        assertEquals((byte) 0x00, packet.getTypeOfService());
        assertEquals((short) 0, packet.getIdentification());
        assertEquals(true, packet.getDontFragment());
        assertEquals(false, packet.getMoreFragments());
        assertEquals((short) 0, packet.getFragmentOffset());
        assertEquals((short) 20 + data.length, packet.getTotalLength());
        assertEquals(source, packet.getSourceAddress());
        assertEquals(destination, packet.getDestinationAddress());
        assertArrayEquals(data, packet.getData());
        assertEquals((short) 0xB9B5, packet.getChecksum());
        short checksum = packet.calculateHeaderChecksum().getValue(); 
        assertEquals((short) 0xFFFF, checksum);

    }

    @Test
    public void test_creation_with_fragment_info() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
        var frag = new IPPacket.FragmentInfo(3,false,true, 13);
        IPPacket packet = new IPPacket(IPPacket.TCP, frag, (byte)64,
            source, destination, data);  
        assertEquals(4, packet.getVersion());
        assertEquals(5, packet.getHeaderLength());
        assertEquals((byte) 0x00, packet.getTypeOfService());
        assertEquals((short) 3, packet.getIdentification());
        assertEquals(false, packet.getDontFragment());
        assertEquals(true, packet.getMoreFragments());
        assertEquals((short) 13, packet.getFragmentOffset());
        assertEquals((short) 20 + data.length, packet.getTotalLength());
        assertEquals(source, packet.getSourceAddress());
        assertEquals(destination, packet.getDestinationAddress());
        assertArrayEquals(data, packet.getData());
        short checksum = packet.calculateHeaderChecksum().getValue(); 
        assertEquals((short) 0xFFFF, checksum);
    }


    @Test
    public void testCalculateHeaderChecksum() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
            IPPacket packet = new IPPacket(
            IPPacket.TCP, 64,
            source, destination, data);
        short headerChecksum = packet.getChecksum();
        assertEquals((short) 0xB9B5, headerChecksum);
        short checksum = packet.calculateHeaderChecksum().getValue(); 
        assertEquals((short) 0xFFFF, checksum);
    }
}
