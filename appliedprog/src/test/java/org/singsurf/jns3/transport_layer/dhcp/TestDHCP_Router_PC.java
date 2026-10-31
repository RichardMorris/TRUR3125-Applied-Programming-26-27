package org.singsurf.jns3.transport_layer.dhcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.HomeRouter;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestDHCP_Router_PC {
	DHCPProtocol routerDHCP;
	HomeRouter router1;
	PC pc1;
	DHCPProtocol pcDHCP;
	
	@BeforeEach
	void setUp() throws Exception {
		router1 = DeviceFactory.getInstance().createHomeRouter("Router1");
		routerDHCP = router1.getDHCPProtocol();

		pc1 = DeviceFactory.getInstance().createPC("PC2");
		pc1.setIPAddress(DeviceFactory.getInstance().createSequentialLocalIPAddress());
		pcDHCP = pc1.getDHCPProtocol();

	}
	
	@Test
	void discover_across_simple_network() {
		var connection = new SimpleConnection(router1.getEndPoint(), pc1.getEndPoint());
		assertNotNull(connection);
//		pc2.getArpProtocol().sendGARPRequest();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		System.out.println("Discover");
		System.out.println("router mac"+ router1.getMACAddress());
		pcDHCP.startDiscovery();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();

			Jns3Logger.clearLog();
			System.out.println("Tick " + 1);
			GlobalWallClock.getInstance().advanceTime();
			System.out.println(Jns3Logger.asString());

		assertEquals(4, Jns3Logger.size());
		assertEquals("[INFO] Router1-DHCP-Discover: Received DHCP Discover message",
			Jns3Logger.getItem(0).toString());
		assertEquals("[INFO] PC2-DHCP-Offer: Received DHCP Offer message YIADDR 192.168.1.3",
			Jns3Logger.getItem(2).toString());
		assertTrue(Jns3Logger.isErrorFree());
		var offerMessage = pcDHCP.lastOfferMessage;
		System.out.println("Offer message: " + offerMessage);
		var yaddr = offerMessage.getYiaddr();
		assertEquals(pc1.getMACAddress(), router1.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(router1.getMACAddress(), pc1.getArpProtocol().lookup(router1.getIPAddress()));
		
		
		Jns3Logger.clearLog();
		System.out.println("Tick " + 2);
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());

		assertEquals(3, Jns3Logger.size());
		assertEquals("[INFO] Router1-DHCP-Request: Received DHCP request message",
			Jns3Logger.getItem(0).toString());
		assertEquals("[INFO] PC2-DHCP-Ack: Received DHCP Ack message YIADDR 192.168.1.3",
				Jns3Logger.getItem(2).toString());

		assertEquals(yaddr, pc1.getIPAddress());
		assertEquals(pc1.getMACAddress(), router1.getArpProtocol().lookup(yaddr));
		assertEquals(router1.getMACAddress(), pc1.getArpProtocol().lookup(router1.getIPAddress()));
		
		assertTrue(Jns3Logger.isErrorFree());
	}

}
