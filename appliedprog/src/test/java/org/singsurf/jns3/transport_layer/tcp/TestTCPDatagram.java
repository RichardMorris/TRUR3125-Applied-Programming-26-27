package org.singsurf.jns3.transport_layer.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.transport_layer.tcp.TCPSegment.PseudoHeader;

class TestTCPDatagram {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test_initial_constructor() {
		var pseudo = new PseudoHeader(
				DeviceFactory.getInstance().createLocalIPAddress(),
				DeviceFactory.getInstance().createLocalIPAddress(),
				(byte) 6, (short) 20);
		var message = "Hello, TCP!";
		
		var datagram = new TCPSegment((short) 1234, (short) 5678,
				message.getBytes(), pseudo);
		
		var expectedHeader = "TCPDatagram[srcPort=1234, destPort=5678, seqNum=0, ackNum=0, DO=0x50, flags=NONE, WS=0, "
				+ "checksum=" + String.format("0x%04X", datagram.getChecksum()) 
				+ ", UP=0, contents=11]";
		assertEquals(expectedHeader, datagram.toHeaderString());
		
		assertFalse(datagram.isSYN());
		assertFalse(datagram.isACK());
		assertFalse(datagram.isFIN());
		assertFalse(datagram.isRST());
		assertFalse(datagram.isPSH());
		
		assertTrue(datagram.isValid());
	}

	@Test
	void test_initial_constructor_with_modified_flags() {
		var pseudo = new PseudoHeader(
				DeviceFactory.getInstance().createLocalIPAddress(),
				DeviceFactory.getInstance().createLocalIPAddress(),
				(byte) 6, (short) 20);
		var message = "Hello, TCP!";
		
		var datagram = new TCPSegment((short) 1234, (short) 5678,
				message.getBytes(), pseudo);
		assertTrue(datagram.isValid());
		datagram.setSYN(true);
		datagram.setACK(true);
		assertTrue(datagram.isValid());
		
		var expectedHeader = "TCPDatagram[srcPort=1234, destPort=5678, seqNum=0, ackNum=0, DO=0x50, flags=SYN-ACK, WS=0, "
				+ "checksum=" + String.format("0x%04X", datagram.getChecksum()) 
				+ ", UP=0, contents=11]";
		assertEquals(expectedHeader, datagram.toHeaderString());
		System.out.println(datagram.toHeaderString());
		
		assertTrue(datagram.isSYN());
		assertTrue(datagram.isACK());
		assertFalse(datagram.isFIN());
		assertFalse(datagram.isRST());
		assertFalse(datagram.isPSH());
		
		datagram.setSYN(false);
		datagram.setACK(false);
		datagram.setFIN(true);
		datagram.setRST(true);
		datagram.setPSH(true);
		
		var expectedHeader2 = "TCPDatagram[srcPort=1234, destPort=5678, seqNum=0, ackNum=0, DO=0x50, flags=FIN-RST-PSH, WS=0, "
				+ "checksum=" + String.format("0x%04X", datagram.getChecksum()) 
				+ ", UP=0, contents=11]";
		assertEquals(expectedHeader2, datagram.toHeaderString());
		System.out.println(datagram.toHeaderString());
		
		assertFalse(datagram.isSYN());
		assertFalse(datagram.isACK());
		assertTrue(datagram.isFIN());
		assertTrue(datagram.isRST());
		assertTrue(datagram.isPSH());
	}

	@Test
	void to_packet_and_back_again() {
		var srcIP = DeviceFactory.getInstance().createLocalIPAddress();
		var destIP = DeviceFactory.getInstance().createLocalIPAddress();
		var pseudo = new PseudoHeader(srcIP, destIP, (byte) 6, (short) 20);
		var message = "Hello, TCP!";
		
		var datagram = new TCPSegment((short) 1234, (short) 5678,
				message.getBytes(), pseudo);
		datagram.setPSH(true);
		datagram.setACK(true);
		
		var expectedHeader = "TCPDatagram[srcPort=1234, destPort=5678, seqNum=0, ackNum=0, DO=0x50, flags=PSH-ACK, WS=0, "
				+ "checksum=" + String.format("0x%04X", datagram.getChecksum()) 
				+ ", UP=0, contents=11]";
		assertEquals(expectedHeader, datagram.toHeaderString());
		System.out.println(datagram.toHeaderString());
		
		assertFalse(datagram.isSYN());
		assertTrue(datagram.isACK());
		assertFalse(datagram.isFIN());
		assertFalse(datagram.isRST());
		assertTrue(datagram.isPSH());
		
		var packet  = new IPPacket(IPPacket.TCP, 64, srcIP, destIP, datagram.getBytes());
		
		var receivedDatagram = TCPSegment.fromPacket(packet);
		assertEquals(expectedHeader, receivedDatagram.toHeaderString());
		
		
	}

}
