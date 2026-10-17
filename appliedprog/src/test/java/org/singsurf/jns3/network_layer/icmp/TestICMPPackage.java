package org.singsurf.jns3.network_layer.icmp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

class TestICMPPackage {
	DeviceFactory deviceFactory = new DeviceFactory();
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void can_create_an_icmp_message() {
		var msg = new ICMPMessage((byte) 0x08, (byte) 0x09, 0x12345678);
		assertEquals((byte) 0x08, msg.getType());
		assertEquals((byte) 0x09, msg.getCode());
		assertEquals(0x12345678, msg.getRestOfHeader());
		assertEquals(OnesComplementNumber.MINUS_ZERO, msg.calculateChecksum());
	}
	
	@Test
	void can_create_a_ping_request() {
		var msg = ICMPMessage.createPingRequest((short) 0x1234, (short) 0x5678);
		assertEquals((byte) 0x08, msg.getType());
		assertEquals((byte) 0x00, msg.getCode());
		assertEquals(0x1234, msg.getIdentifier());
		assertEquals(0x5678, msg.getSequenceNumber());
		assertEquals(OnesComplementNumber.MINUS_ZERO, msg.calculateChecksum());
	}

	@Test
	void can_create_a_ping_reply() {
		var msg = ICMPMessage.createPingReply(0x5678,0x1234);
		assertEquals((byte) 0x00, msg.getType());
		assertEquals((byte) 0x00, msg.getCode());
		assertEquals(0x5678, msg.getIdentifier());
		assertEquals(0x1234, msg.getSequenceNumber());
		assertEquals(OnesComplementNumber.MINUS_ZERO, msg.calculateChecksum());
	}

	@Test
	void can_create_an_ICMPErrorMessage() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x34, (byte) 0x12, (byte) 0x78, (byte) 0x56 };
		var msg = new ICMPErrorMessage((byte) 12, (byte) 0, 0x12345678, data);
		assertEquals((byte) 12, msg.getType());
		assertEquals((byte) 0x00, msg.getCode());
		assertEquals(0x12345678, msg.getRestOfHeader());
		assertEquals(OnesComplementNumber.MINUS_ZERO, msg.calculateChecksum());
	}

	@Test
	void can_create_an_ICMPErrorMessage_from_a_packet() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x34, (byte) 0x12, (byte) 0x78, (byte) 0x56 };
		var senderIP = deviceFactory.createLocalIPAddress();
		var destinationIP = deviceFactory.createLocalIPAddress();

		var packet = new IPPacket(IPPacket.IMCP, 64, senderIP, destinationIP, data);
		var msg = ICMPErrorMessage.fromPacket(packet);
		assertEquals((byte) 12, msg.getType());
		assertEquals((byte) 0x02, msg.getCode());
//		assertEquals(0x12345678, msg.getRestOfHeader());
		assertEquals(OnesComplementNumber.MINUS_ZERO, msg.calculateChecksum());
	}

	@Test
	public void can_wrap_and_unwrap_a_mesage_in_a_packet() {
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x34, (byte) 0x12, (byte) 0x78, (byte) 0x56 };
		var msg = new ICMPMessage((byte) 0x08, (byte) 0x09, 0xFEDCBA98, data);
		
		var senderIP = deviceFactory.createLocalIPAddress();
		var destinationIP = deviceFactory.createLocalIPAddress();
		var ttl = 64;
		
		IPPacket packet = new IPPacket(
				IPPacket.IMCP,ttl,
				senderIP, destinationIP, msg.getBytes());
		ICMPMessage reply = ICMPMessage.fromPacket(packet);
		
		assertEquals((byte) 0x08, reply.getType());
		assertEquals((byte) 0x09, reply.getCode());
		assertEquals(0xFEDCBA98, reply.getRestOfHeader());
		var replyData = reply.getData();
		assertArrayEquals(data, replyData);
		assertEquals(OnesComplementNumber.MINUS_ZERO, reply.calculateChecksum());
	}

}
