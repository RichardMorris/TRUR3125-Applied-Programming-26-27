package org.singsurf.jns3.transport_layer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

class TestUDPDatagram {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void checksum_valid_for_creation_with_psudo_header() {
		var srcIP = DeviceFactory.getInstance().createLocalIPAddress();
		var destIP = DeviceFactory.getInstance().createLocalIPAddress();
		short srcPort = 1234;
		short destPort = 5678;
		byte protocol = 17; // UDP
		var psudoHeader = new UDPDatagram.PsudoHeader(srcIP, destIP, protocol, (short) 8);
		byte[] message = new byte[] { 0x01, 0x02, 0x03, 0x04 };
		var udpDatagram = new UDPDatagram(srcPort, destPort, message, psudoHeader);
		assertTrue(udpDatagram.isValid());
	}

}
