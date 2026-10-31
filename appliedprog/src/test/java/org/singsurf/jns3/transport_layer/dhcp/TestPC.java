package org.singsurf.jns3.transport_layer.dhcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.physical_layer.SimpleConnection;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

class TestPC {
	DHCPProtocol dhcpProtocol;
	private PC pc1;
	private PC pc2;
	private DHCPProtocol dhcpProtocol2;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = DeviceFactory.getInstance().createPC("PC1");
		pc1.setIPAddress(DeviceFactory.getInstance().createSequentialLocalIPAddress());
		dhcpProtocol = new DHCPProtocol(pc1, true);
		var udpProtocol = pc1.getUDPProtocol();
		udpProtocol.register(dhcpProtocol);

		pc2 = DeviceFactory.getInstance().createPC("PC2");
		pc2.setIPAddress(DeviceFactory.getInstance().createSequentialLocalIPAddress());
		dhcpProtocol2 = new DHCPProtocol(pc2, false);
		var udpProtocol2 = pc2.getUDPProtocol();
		udpProtocol2.register(dhcpProtocol2);

	}
	
	@Test
	void discover_message_direct_to_pc() {		
		
		
		Jns3Logger.clearLog();
		var msg = DHCPMessage.createDiscoverMessage(dhcpProtocol.device.getMACAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, DHCPProtocol.DHCP_SERVER_PORT, msg.getBytes());
		var packet = new IPPacket(IPPacket.UDP, 64, 
				pc1.getIPAddress(), 
				IPProtocol.LIMITED_MULTICAST_ADDRESS, 
				datagram.getBytes());
		
		pc1.getIPProtocol().receive(packet);
		dhcpProtocol.onTick();
		System.out.println(Jns3Logger.asString());
		assertEquals(2, Jns3Logger.size());
		assertEquals("[INFO] DiscoverHandler: Received DHCP Discover message"
				+ "", Jns3Logger.getItem(1).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void discover_across_simple_network() {		
		var connection = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		dhcpProtocol2.startDiscovery();

			Jns3Logger.clearLog();
			GlobalWallClock.getInstance().advanceTime();
		
		assertEquals(1, Jns3Logger.size());
		assertEquals("[INFO] DiscoverHandler: Received DHCP Discover message"
				+ "", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

}
