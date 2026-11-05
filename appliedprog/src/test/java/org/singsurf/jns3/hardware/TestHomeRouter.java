package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestHomeRouter {
	PC pc1;
	PC pc2;
	PC pc3;
	PC pc4;
	HomeRouter router;
	
	@BeforeEach
	void setUp() throws Exception {
		DeviceFactory deviceFactory = DeviceFactory.getInstance();
		pc1 = deviceFactory.createPC("PC1");
		pc2 = deviceFactory.createPC("PC2");
		pc3 = deviceFactory.createPC("PC3");
		pc4 = deviceFactory.createPC("PC4");
		
		router = new HomeRouter("HomeRouter", pc1.getMACAddress(), pc1.getIPAddress());
		
		var connection1 = new SimpleConnection(router.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new SimpleConnection(router.getEndPoint(1), pc2.getEndPoint());
		var connection3 = new SimpleConnection(router.getEndPoint(2), pc3.getEndPoint());
		var connection4 = new SimpleConnection(router.getEndPoint(3), pc4.getEndPoint());
		
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
	}

	@Test
	void can_send_garp_request() {
		pc1.getArpProtocol().sendGARPRequest();
		assertEquals(pc2.getMACAddress(), pc1.getArpProtocol().lookup(pc2.getIPAddress()));
		assertEquals(pc1.getMACAddress(), pc4.getArpProtocol().lookup(pc1.getIPAddress()));
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void can_send_frame_to_known_mac() {
		pc1.getArpProtocol().sendGARPRequest();
		Jns3Logger.clearLog();
		byte[] payload = "Hello".getBytes();
		short etherType = (short) payload.length;
		var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
		var fs1 = pc1.getEthernetProtocol().getFrameSender();
		fs1.send(frame);
		assertEquals(1, Jns3Logger.size());
		assertEquals("PC2", Jns3Logger.getItem(0).deviceName());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void can_broardcast_frame() {
		pc1.getArpProtocol().sendGARPRequest();
		Jns3Logger.clearLog();
		byte[] payload = "Hello".getBytes();
		short etherType = (short) payload.length;
		var frame = new EthernetFrame(MACAddress.BROADCAST_MAC, pc1.getMACAddress(), etherType, payload);
		var fs1 = pc1.getEthernetProtocol().getFrameSender();
		fs1.send(frame);
		assertEquals(3, Jns3Logger.size());
		assertEquals("PC2", Jns3Logger.getItem(0).deviceName());
		assertEquals("PC3", Jns3Logger.getItem(1).deviceName());
		assertEquals("PC4", Jns3Logger.getItem(2).deviceName());
		assertTrue(Jns3Logger.isErrorFree());
	}

}
