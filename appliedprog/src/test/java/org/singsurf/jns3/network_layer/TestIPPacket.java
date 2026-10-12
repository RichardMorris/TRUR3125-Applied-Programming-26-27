package org.singsurf.jns3.network_layer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.physical_layer.SimpleConnection;
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
        assertEquals(true, packet.isDontFragment());
        assertEquals(false, packet.isMoreFragments());
        assertEquals((short) 0, packet.getFragmentOffset());
        assertEquals((short) 20 + data.length, packet.getTotalLength());
        assertEquals(source, packet.getSourceAddress());
        assertEquals(destination, packet.getDestinationAddress());
        assertArrayEquals(data, packet.getData());
        assertEquals((short) 0xB9B5, packet.getChecksum());
        short checksum = packet.calculateHeaderChecksum().getValue(); 
        assertEquals((short) 0xFFFF, checksum);
        assertTrue(packet.isValidChecksum());
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
        assertEquals(false, packet.isDontFragment());
        assertEquals(true, packet.isMoreFragments());
        assertEquals((short) 13, packet.getFragmentOffset());
        assertEquals((short) 20 + data.length, packet.getTotalLength());
        assertEquals(source, packet.getSourceAddress());
        assertEquals(destination, packet.getDestinationAddress());
        assertArrayEquals(data, packet.getData());
        short checksum = packet.calculateHeaderChecksum().getValue(); 
        assertEquals((short) 0xFFFF, checksum);
        assertTrue(packet.isValidChecksum());
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
        assertTrue(packet.isValidChecksum());
    }

    @Test
    public void to_bytes_and_back_again() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress destination = new IPAddress(new int[] {192, 168, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, destination, data);

        byte[] bytes = packet.toBytes();

        IPPacket reconstructed =IPPacket.fromBytes(bytes);
 
        assertEquals(packet.getVersion(), reconstructed.getVersion());
        assertEquals(packet.getHeaderLength(), reconstructed.getHeaderLength());
        assertEquals(packet.getTypeOfService(), reconstructed.getTypeOfService());
        assertEquals(packet.getTotalLength(), reconstructed.getTotalLength());
        assertEquals(packet.getIdentification(), reconstructed.getIdentification());
        assertEquals(packet.isDontFragment(), reconstructed.isDontFragment());
        assertEquals(packet.isMoreFragments(), reconstructed.isMoreFragments());
        assertEquals(packet.getFragmentOffset(), reconstructed.getFragmentOffset());
        assertEquals(packet.getChecksum(), reconstructed.getChecksum());
        assertEquals(packet.getSourceAddress(), reconstructed.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), reconstructed.getDestinationAddress());
        assertArrayEquals(packet.getData(), reconstructed.getData());
        assertTrue(reconstructed.isValidChecksum());
    }
    

    @Test
    public void transmit_single_packet() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        var pc1 = deviceFactory.createPC("PC1");
        var pc2 = deviceFactory.createPC("PC2");
        pc2.clearFrameHandlers();
        pc2.addFrameHandler((frame) -> {
			var packet = IPPacket.fromBytes(frame.getPayload());
			assertEquals(packet.getSourceAddress(), pc1.getIPAddress());
			assertEquals(packet.getDestinationAddress(), pc2.getIPAddress());
			assertArrayEquals(packet.getData(), new byte[] {1, 2, 3, 4});
			assertTrue(packet.isValidChecksum());
			Jns3Logger.log(pc2.getName(), Jns3Logger.LogLevel.RECEIVED,"Received packet");
			return true;
		});
        var connection = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
        assertNotNull(connection);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
        
        byte[] data = new byte[] {1, 2, 3, 4};
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), data);
        var frame = pc1.createEthernetFrame(pc2.getMACAddress(), 
        		packet.toBytes());
        pc1.sendFrame(frame);
        assertEquals(Jns3Logger.getLogItems().size(), 1);
        var logItem = Jns3Logger.getLogItems().get(0);
        assertEquals(new Jns3Logger.LogItem(pc2.getName(), Jns3Logger.LogLevel.RECEIVED, "Received packet"), logItem);
//        assertEquals(logItem.message, "Received packet");
//        assertEquals(logItem.level, Jns3Logger.LogLevel.RECEIVED);
//        assertEquals(logItem.deviceName, pc2.getName());        
    }


}
