package org.singsurf.jns3.transport_layer.dhcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.HomeRouter0;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.physical_layer.BufferedConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestDHCP_Router_PC {
	DHCPProtocol routerDHCP;
	HomeRouter0 router1;
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
	void discover_across_buffered_network() {
		var connection = new BufferedConnection(router1.getEndPoint(), pc1.getEndPoint());
		assertNotNull(connection);
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		System.out.println("Discover");
		pcDHCP.startDiscovery();
		System.out.println(Jns3Logger.asString());

		for(int i=0; i<6; i++) {
			Jns3Logger.clearLog();
			System.out.println("Tick " + i);
			GlobalWallClock.getInstance().advanceTime();
			System.out.println(Jns3Logger.asString());
		}
		IPAddress yaddr = null;
		for(var ent : pcDHCP.acceptedOfferMessages.entrySet()) {
			yaddr = ent.getValue().getYiaddr();
			break;
		}
		assertEquals(yaddr, pc1.getIPAddress());
		assertEquals(pc1.getMACAddress(), router1.getArpProtocol().lookup(yaddr));
		assertEquals(router1.getMACAddress(), pc1.getArpProtocol().lookup(router1.getIPAddress()));
		
		assertEquals(0, routerDHCP.offerSockets.size());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void discover_across_simple_network() {
		var connection = new SimpleConnection(router1.getEndPoint(), pc1.getEndPoint());
		assertNotNull(connection);
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

		DHCPMessage offerMessage = null;
		IPAddress yaddr = null;
		for(var ent : pcDHCP.acceptedOfferMessages.entrySet()) {
				yaddr = ent.getValue().getYiaddr();
				offerMessage = ent.getValue();
				break;
		}
		assertTrue(Jns3Logger.isErrorFree());
		System.out.println("Offer message: " + offerMessage);
		assertEquals(pc1.getMACAddress(), router1.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(router1.getMACAddress(), pc1.getArpProtocol().lookup(router1.getIPAddress()));
		
		
		Jns3Logger.clearLog();
		System.out.println("Tick " + 2);
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());

		assertEquals(yaddr, pc1.getIPAddress());
		assertEquals(pc1.getMACAddress(), router1.getArpProtocol().lookup(yaddr));
		assertEquals(router1.getMACAddress(), pc1.getArpProtocol().lookup(router1.getIPAddress()));
		
		assertEquals(0, routerDHCP.offerSockets.size());
		
		
		assertTrue(Jns3Logger.isErrorFree());
	}

}
