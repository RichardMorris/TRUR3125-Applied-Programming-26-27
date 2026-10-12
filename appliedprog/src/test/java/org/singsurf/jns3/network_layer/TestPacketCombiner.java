package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;

class TestPacketCombiner {
	PacketFragmentor fragmentor = new PacketFragmentor(100);
	List<IPPacket> recivedPackets = new ArrayList<>();
	
	class DummyReceiver implements PacketReceiver {
		@Override
		public void receivePacket(IPPacket packet) {
			recivedPackets.add(packet);
			Jns3Logger.log("DummyReceiver", Jns3Logger.LogLevel.RECEIVED, "Received packet: " + packet);
		}
	}
	PC pc;
	FragmentCombiner combiner;

	@BeforeEach
	void setUp() throws Exception {
		var deviceFactory = new DeviceFactory();
		pc = deviceFactory.createPC("TestPC");
		combiner = new FragmentCombiner(pc,new DummyReceiver());
	}

	@Test
	void small_packets_unchanged() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        byte[] data = new byte[] {1, 2, 3, 4};
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(1, fragments.size());
        
        Jns3Logger.clearLog();
        combiner.handlePacket(fragments.get(0));
        assertEquals(1, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());
	}

	
	@Test
	void split_packets_in_two() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        
        byte[] data = new byte[150];
        for(int i=0;i<data.length;i++) data[i] = (byte) i;
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(2, fragments.size());
        var frag1 = fragments.get(0);
        var frag2 = fragments.get(1);
        
        Jns3Logger.clearLog();
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag2);
        assertEquals(1, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void split_packets_in_two_reverse_order() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        
        byte[] data = new byte[150];
        for(int i=0;i<data.length;i++) data[i] = (byte) i;
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);
        var fragments = fragmentor.getFragments(packet);
        assertEquals(2, fragments.size());
        var frag1 = fragments.get(0);
        var frag2 = fragments.get(1);
        
        Jns3Logger.clearLog();
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag1);
        assertEquals(1, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void split_packets_in_three() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        
        byte[] data = new byte[200];
        for(int i=0;i<data.length;i++) data[i] = (byte) (i & 0xFF);
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);

        Jns3Logger.clearLog();
        var fragments = fragmentor.getFragments(packet);
        assertEquals(3, fragments.size());
        var frag1 = fragments.get(0);
        var frag2 = fragments.get(1);
        var frag3 = fragments.get(2);
        recivedPackets.clear();
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag3);
        assertEquals(1, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());


        recivedPackets.clear();
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag2);
        assertEquals(1, recivedPackets.size());
        receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        recivedPackets.clear();
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag3);
        assertEquals(1, recivedPackets.size());
        receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        recivedPackets.clear();
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag1);
        assertEquals(1, recivedPackets.size());
        receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        recivedPackets.clear();
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag2);
        assertEquals(1, recivedPackets.size());
        receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        recivedPackets.clear();
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag1);
        assertEquals(1, recivedPackets.size());
        receivedPacket = recivedPackets.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());
	}
	
	@Test
	public void intermingled_packets_with_different_identifaction() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        
        byte[] data = new byte[200];
        for(int i=0;i<data.length;i++) data[i] = (byte) (i & 0xFF);
        IPPacket packet1 = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);
        
        byte[] data2 = new byte[150];
        for(int i=0;i<data2.length;i++) data2[i] = (byte) i;
        IPPacket packet2 = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data2);

        var fragments1 = fragmentor.getFragments(packet1);
        var fragments2 = fragmentor.getFragments(packet2);
        assertEquals(3, fragments1.size());
        assertEquals(2, fragments2.size());

        recivedPackets.clear();
        combiner.handlePacket(fragments1.get(0));
        combiner.handlePacket(fragments2.get(0));
        combiner.handlePacket(fragments1.get(1));
        combiner.handlePacket(fragments2.get(1));
        combiner.handlePacket(fragments1.get(2));
        
        
        assertEquals(2, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet2.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet2.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet2.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet2.getData(), receivedPacket.getData());
        
        receivedPacket = recivedPackets.get(1);
        assertEquals(packet1.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet1.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet1.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet1.getData(), receivedPacket.getData());
        
        assertTrue(Jns3Logger.isErrorFree());

        
        Jns3Logger.clearLog();

	}

	@Test
	public void intermingled_packets_with_different_sources() {
        IPAddress source = new IPAddress(new byte[] {127, 0, 0, 1});
        IPAddress source2 = new IPAddress(new int[] {138, 253, 39, 2});
        
        byte[] data = new byte[200];
        for(int i=0;i<data.length;i++) data[i] = (byte) (i & 0xFF);
        IPPacket packet1 = new IPPacket(IPPacket.TCP, 64,
            source, pc.getIPAddress(), data);
        
        byte[] data2 = new byte[150];
        for(int i=0;i<data2.length;i++) data2[i] = (byte) i;
        IPPacket packet2 = new IPPacket(IPPacket.TCP, 64,
            source2, pc.getIPAddress(), data2);

        var fragments1 = fragmentor.getFragments(packet1);
        var fragments2 = fragmentor.getFragments(packet2);
        assertEquals(3, fragments1.size());
        assertEquals(2, fragments2.size());

        recivedPackets.clear();
        combiner.handlePacket(fragments1.get(0));
        combiner.handlePacket(fragments2.get(0));
        combiner.handlePacket(fragments1.get(1));
        combiner.handlePacket(fragments2.get(1));
        combiner.handlePacket(fragments1.get(2));
        
        
        assertEquals(2, recivedPackets.size());
        var receivedPacket = recivedPackets.get(0);
        assertEquals(packet2.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet2.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet2.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet2.getData(), receivedPacket.getData());
        
        receivedPacket = recivedPackets.get(1);
        assertEquals(packet1.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet1.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet1.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet1.getData(), receivedPacket.getData());
        
        assertTrue(Jns3Logger.isErrorFree());

        
        Jns3Logger.clearLog();

	}

}
