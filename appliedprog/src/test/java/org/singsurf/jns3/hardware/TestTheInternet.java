package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.ChatApplication;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.physical_layer.BufferedConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestTheInternet {

	TheInternet theInternet;
	HomeRouter router1;
	PC pc1;
	PC pc2;
	PC pc3;
	PC pc4;
	HomeRouter router2;
	PC pc5;
	
	@BeforeEach
	void setUp() throws Exception {
		DeviceFactory deviceFactory = DeviceFactory.getInstance();
		theInternet = deviceFactory.createTheInternet("TheInternet");
		router1 = deviceFactory.createHomeRouter("HR1");
		pc1 = deviceFactory.createPC("PC1",IPAddress.ZERO_ADDRESS);
		pc2 = deviceFactory.createPC("PC2",IPAddress.ZERO_ADDRESS);	
		pc3 = deviceFactory.createPC("PC3",IPAddress.ZERO_ADDRESS);
		pc4 = deviceFactory.createPC("PC4",IPAddress.ZERO_ADDRESS);

		router2 = deviceFactory.createHomeRouter("HR2");
		pc5 = deviceFactory.createPC("PC5",IPAddress.ZERO_ADDRESS);
		
		var connection1 = new SimpleConnection(router1.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new SimpleConnection(router1.getEndPoint(1), pc2.getEndPoint());
		var connection3 = new SimpleConnection(router1.getEndPoint(2), pc3.getEndPoint());
		var connection4 = new SimpleConnection(router1.getEndPoint(3), pc4.getEndPoint());
		var connection5 = new BufferedConnection(router1.getEndPoint(4), theInternet.getEndPoint(0));

		var connection6 = new BufferedConnection(router2.getEndPoint(4), theInternet.getEndPoint(1));
		var connection7 = new SimpleConnection(router2.getEndPoint(2), pc5.getEndPoint());

		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		assertNotNull(connection5);
		assertNotNull(connection6);
		assertNotNull(connection7);

		theInternet.registerDevice(0, router1);
		router1.registerDevice(4, theInternet);
		
		theInternet.registerDevice(1, router2);
		router2.registerDevice(4, theInternet);

		pc1.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc2.getDHCPProtocol().startDiscovery();
		pc5.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc3.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		pc4.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		
		Jns3Logger.println();
		assertTrue(Jns3Logger.isErrorFree());

		ChatApplication.showPrompts = false;
	}
	
	@Test
	void testSetup() {
		var prefix = theInternet.getPrefix();
		var s = prefix.toString();
		assertEquals("0.0.0.0/0", s);

		var pub = theInternet.getPublicIP();
		assertEquals("195.66.226.16", pub.toString());
		
		assertEquals("255.255.255.255", theInternet.getSubnetMask().toString());
		
		prefix = router1.getPrefix();
		s = prefix.toString();
		assertTrue(s.startsWith("81."));
		assertTrue(s.endsWith(".0/24"));

		pub = router1.getPublicIP();
		var pubStr = pub.toString();
		assertTrue(pubStr.startsWith("81."));
		assertTrue(pubStr.endsWith(".0"));
		assertEquals("255.255.255.0", router1.getSubnetMask().toString());

		prefix = router1.getPrefix();
		s = prefix.toString();
		assertTrue(s.startsWith("81."));
		assertTrue(s.endsWith(".0/24"));

		var pub2 = router2.getPublicIP();
		pubStr = pub2.toString();
		assertTrue(pubStr.startsWith("81."));
		assertTrue(pubStr.endsWith(".0"));
		assertEquals("255.255.255.0", router1.getSubnetMask().toString());

		assertNotEquals(pub, pub2);
		System.out.println(theInternet);
		System.out.println(router1);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);
		System.out.println(router2);
		System.out.println(pc5);
		
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
		GlobalWallClock.advanceBy(10);
		Jns3Logger.println();
		assertEquals(1, Jns3Logger.size());
		assertEquals("TheInternet", Jns3Logger.getItem(0).deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, 
				Jns3Logger.getItem(0).level());
	}

	@Test
	public void can_ping_the_internet() throws IOException {		
		Jns3Logger.clearLog();
		System.out.println(theInternet);
		System.out.println(router1);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

		Jns3Logger.clearLog();
		Jns3Logger.startCapture();
		pc1.getICMPProtocol().sendPingRequest(theInternet.getIPAddress(), 64);
		GlobalWallClock.advanceBy(10);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.println("Captured output:\n"+output);
		var lines = output.split("\n");
		assertEquals(2, lines.length);
		assertEquals("PING " + theInternet.getIPAddress() + " 28 bytes of data.", lines[0].trim());
		assertEquals("28 bytes from "+theInternet.getIPAddress()+": icmp_seq=1 ttl=63 time=2 ticks", lines[1].trim());

		assertEquals(2, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		var item1 = Jns3Logger.getItem(1);
		assertEquals("TheInternet", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received ping request from 81."));
		assertEquals("PC1", item1.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item1.level());
		assertTrue(item1.message().startsWith("Received ping reply from "+theInternet.getIPAddress()));
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void can_ping_across_the_internet() throws IOException {		
		System.out.println(theInternet);
		System.out.println(router1);
		System.out.println(pc1);
		System.out.println(router2);
		System.out.println(pc5);

		var pc5pub = router2.getPublicIP(pc5.getIPAddress());
		
		Jns3Logger.clearLog();
		Jns3Logger.startCapture();
		pc1.getICMPProtocol().sendPingRequest(pc5pub, 64);
		GlobalWallClock.advanceBy(10);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.println("Captured output:\n"+output);
		var lines = output.split("\n");
		assertEquals(2, lines.length);
		assertEquals("PING " + pc5pub + " 28 bytes of data.", lines[0].trim());
		assertEquals("28 bytes from "+pc5pub+": icmp_seq=1 ttl=61 time=4 ticks", lines[1].trim());

		assertEquals(2, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		var item1 = Jns3Logger.getItem(1);
		assertEquals("PC5", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received ping request from 81."));
		assertEquals("PC1", item1.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item1.level());
		assertTrue(item1.message().startsWith("Received ping reply from "+pc5pub));
		assertTrue(Jns3Logger.isErrorFree());
	}
	
	@AfterEach
	void tearDown() throws Exception {
		assertTrue(Jns3Logger.isErrorFree());
	}

}
