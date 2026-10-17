package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestPacketCombiner {
	PacketFragmentor fragmentor = new PacketFragmentor(100);

	PC pc;
	FragmentCombiner combiner;

	private DefaultPacketReceiver receiver;
	DeviceFactory deviceFactory = new DeviceFactory();

	@BeforeEach
	void setUp() throws Exception {
		pc = deviceFactory.createPC("TestPC");
		receiver = new DefaultPacketReceiver(pc.getName());
		combiner = new FragmentCombiner(pc,receiver);
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
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
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
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
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
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
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
        receiver.clear();
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag3);
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        receiver.clear();
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag2);
        assertEquals(1, receiver.size());
        receivedPacket = receiver.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        receiver.clear();
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag3);
        assertEquals(1, receiver.size());
        receivedPacket = receiver.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        receiver.clear();
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag1);
        assertEquals(1, receiver.size());
        receivedPacket = receiver.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        receiver.clear();
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag1);
        combiner.handlePacket(frag2);
        assertEquals(1, receiver.size());
        receivedPacket = receiver.get(0);
        assertEquals(packet.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        assertTrue(Jns3Logger.isErrorFree());

        receiver.clear();
        combiner.handlePacket(frag3);
        combiner.handlePacket(frag2);
        combiner.handlePacket(frag1);
        assertEquals(1, receiver.size());
        receivedPacket = receiver.get(0);
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

        Jns3Logger.clearLog();
        receiver.clear();
        combiner.handlePacket(fragments1.get(0));
        combiner.handlePacket(fragments2.get(0));
        combiner.handlePacket(fragments1.get(1));
        combiner.handlePacket(fragments2.get(1));
        combiner.handlePacket(fragments1.get(2));
        
        
        assertEquals(2, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(packet2.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet2.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet2.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet2.getData(), receivedPacket.getData());
        
        receivedPacket = receiver.get(1);
        assertEquals(packet1.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet1.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet1.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet1.getData(), receivedPacket.getData());
        
        assertTrue(Jns3Logger.isErrorFree());
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

        receiver.clear();
        combiner.handlePacket(fragments1.get(0));
        combiner.handlePacket(fragments2.get(0));
        combiner.handlePacket(fragments1.get(1));
        combiner.handlePacket(fragments2.get(1));
        combiner.handlePacket(fragments1.get(2));
        
        
        assertEquals(2, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(packet2.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet2.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet2.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet2.getData(), receivedPacket.getData());
        
        receivedPacket = receiver.get(1);
        assertEquals(packet1.getSourceAddress(), receivedPacket.getSourceAddress());
        assertEquals(packet1.getDestinationAddress(), receivedPacket.getDestinationAddress());
        assertEquals(packet1.getProtocol(), receivedPacket.getProtocol());
        assertArrayEquals(packet1.getData(), receivedPacket.getData());
        
        assertTrue(Jns3Logger.isErrorFree());
	}
	
	@Test
	public void a_small_packet_can_be_transmission_over_a_simple_network() {
		// Arrange
		PC pc2 = deviceFactory.createPC("Sender PC");
		pc.clearFrameHandlers();
		pc.addFrameHandler(pc.getArpController());
		pc.addFrameHandler(combiner);
		var connection = new SimpleConnection(pc.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		pc.sendGARPRequest();
		// Act
		var packet = pc2.createIPPacket(IPPacket.TCP,pc.getIPAddress(), 
				new byte[] {1,2,3,4,5,6,7,8,9,10});
		pc2.sendPacket(packet);
		// Assert
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(pc2.getIPAddress(), receivedPacket.getSourceAddress());
        assertEquals(pc.getIPAddress(), receivedPacket.getDestinationAddress());
        assertEquals(IPPacket.TCP, receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());

	}

	@Test
	public void a_large_packet_can_be_transmission_over_a_simple_network() {
		// Arrange
		PC pc2 = deviceFactory.createPC("Sender PC");
		pc.clearFrameHandlers(); // Need set our own frame handlers to protect against later changes.
		pc.addFrameHandler(pc.getArpController());
		pc.addFrameHandler(combiner);
		var connection = new SimpleConnection(pc.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		pc.sendGARPRequest();
		
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
				pc.getIPAddress(), 
				dataString);
		pc2.sendPacket(packet);
		
		// Assert
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(pc2.getIPAddress(), receivedPacket.getSourceAddress());
        assertEquals(pc.getIPAddress(), receivedPacket.getDestinationAddress());
        assertEquals(IPPacket.TCP, receivedPacket.getProtocol());
        assertArrayEquals(packet.getData(), receivedPacket.getData());
        System.out.println(receivedPacket.getDataAsString());
        assertTrue(Jns3Logger.isErrorFree());

	}

}
