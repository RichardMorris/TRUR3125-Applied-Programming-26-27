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
	DHCPProtocol dhcpProtocol1;
	private HomeRouter router1;
	private PC pc2;
	private DHCPProtocol dhcpProtocol2;
	
	@BeforeEach
	void setUp() throws Exception {
		router1 = DeviceFactory.getInstance().createHomeRouter("Router1");
		dhcpProtocol1 = router1.getDHCPProtocol();

		pc2 = DeviceFactory.getInstance().createPC("PC2");
		pc2.setIPAddress(DeviceFactory.getInstance().createSequentialLocalIPAddress());
		dhcpProtocol2 = pc2.getDHCPProtocol();

	}
	
	@Test
	void discover_across_simple_network() {		
		var connection = new SimpleConnection(router1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		pc2.getArpProtocol().sendGARPRequest();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		System.out.println("Discover");
		
		dhcpProtocol2.startDiscovery();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();

			Jns3Logger.clearLog();
			System.out.println("Tick " + 1);
			GlobalWallClock.getInstance().advanceTime();
			System.out.println(Jns3Logger.asString());

		assertEquals(3, Jns3Logger.size());
		assertEquals("[INFO] Router1-DHCP-Discover: Received DHCP Discover message",
			Jns3Logger.getItem(0).toString());
		assertEquals("[INFO] PC2-DHCP-Offer: Received DHCP Offer message YIADDR 192.168.1.3",
			Jns3Logger.getItem(2).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

}
