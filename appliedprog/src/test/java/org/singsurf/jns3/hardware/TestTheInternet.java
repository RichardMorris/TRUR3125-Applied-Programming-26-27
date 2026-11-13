package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.ChatApplication;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestTheInternet {

	PC pc1;
	PC pc2;
	PC pc3;
	PC pc4;
	HomeRouter router;
	TheInternet theInternet;
	
	@BeforeEach
	void setUp() throws Exception {
		DeviceFactory deviceFactory = DeviceFactory.getInstance();
		pc1 = deviceFactory.createPC("PC1");
		pc2 = deviceFactory.createPC("PC2");
		pc3 = deviceFactory.createPC("PC3");
		pc4 = deviceFactory.createPC("PC4");
		router = deviceFactory.createHomeRouter("HomeRouter");
		theInternet = deviceFactory.createTheInternet("TheInternet");
		pc1.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc2.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc3.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc4.setIPAddress(IPAddress.ZERO_ADDRESS);
		
		var connection1 = new SimpleConnection(router.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new SimpleConnection(router.getEndPoint(1), pc2.getEndPoint());
		var connection3 = new SimpleConnection(router.getEndPoint(2), pc3.getEndPoint());
		var connection4 = new SimpleConnection(router.getEndPoint(3), pc4.getEndPoint());
		var connection5 = new SimpleConnection(router.getEndPoint(4), theInternet.getEndPoint(0));
		
		theInternet.registerDevice(0, router);
		router.registerDevice(4, theInternet);
		
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		assertNotNull(connection5);
		
		pc1.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc2.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc3.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc4.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		assertTrue(Jns3Logger.isErrorFree());

		ChatApplication.showPrompts = false;
	}
	
	@Test
	void testSetup() {
		var prefix = router.getPrefix();
		var s = prefix.toString();
		assertTrue(s.startsWith("81."));
		assertTrue(s.endsWith(".0/24"));
		
		prefix = theInternet.getPrefix();
		s = prefix.toString();
		assertEquals("0.0.0.0/0", s);
		
		var pub = theInternet.getPublicIP();
		assertEquals("195.66.226.16", pub.toString());
		
		assertEquals("255.255.255.255", theInternet.getSubnetMask().toString());
		System.out.println(theInternet);

		pub = router.getPublicIP();
		var pubStr = pub.toString();
		assertTrue(pubStr.startsWith("81."));
		assertTrue(pubStr.endsWith(".0"));
		assertEquals("255.255.255.0", router.getSubnetMask().toString());

		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);
		
		Jns3Logger.println();
//		Jns3Logger.clearLog();
	}

	@Test
	void send_packet_to_the_internet() {
		Jns3Logger.clearLog();
		byte[] payload = "Hello from PC1".getBytes();
		
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            pc1.getIPAddress(), theInternet.getIPAddress(), payload);
		
		var fs1 = pc1.getIPProtocol().getPacketSender();
		fs1.send(packet);
		Jns3Logger.println();
		assertEquals(1, Jns3Logger.size());
		assertEquals("TheInternet", Jns3Logger.getItem(0).deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, 
				Jns3Logger.getItem(0).level());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@AfterEach
	void tearDown() throws Exception {
		assertTrue(Jns3Logger.isErrorFree());
	}

}
