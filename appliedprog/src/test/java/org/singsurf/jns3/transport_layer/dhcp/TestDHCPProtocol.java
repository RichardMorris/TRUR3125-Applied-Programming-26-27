package org.singsurf.jns3.transport_layer.dhcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage.DHCPOption;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

class TestDHCPProtocol {
	DHCPProtocol dhcpProtocol;
	
	@BeforeEach
	void setUp() throws Exception {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ip = DeviceFactory.getInstance().createLocalIPAddress();
		dhcpProtocol = new DHCPProtocol(AddressableDevice.createAddressableDevice("PC1", mac, ip), false);
		var udpProtocol = new UDPProtocol(dhcpProtocol.device);
		udpProtocol.register(dhcpProtocol);
	}

	@Test
	void discover_message_direct_to_delegator() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ciaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var yiaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var siaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var giaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var addresses = new DHCPMessage.DHCPAddresses(mac, ciaddr, yiaddr, siaddr, giaddr);
		byte op = DHCPProtocol.DHCP_OP_DISCOVER;
		int xid = 0x3903F326;
		short secs = 0;
		short flags = 0;
		List<DHCPOption> options = new ArrayList<>();
		options.add(new DHCPOption((byte) 53, (byte) 1, new byte[] { (byte) DHCPProtocol.DHCP_OP_DISCOVER }));
		options.add(new DHCPOption((byte) 55, (byte) 4, new byte[] { (byte) 1, (byte) 3, (byte) 6, (byte) 15 }));
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);

		Jns3Logger.clearLog();
		dhcpProtocol.delgator.receive(message);
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.size());
		assertEquals("[INFO] DiscoverHandler: Received DHCP Discover message"
				+ "", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void discover_message_via_socket() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ciaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var yiaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var siaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var giaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var addresses = new DHCPMessage.DHCPAddresses(mac, ciaddr, yiaddr, siaddr, giaddr);
		byte op = DHCPProtocol.DHCP_OP_DISCOVER;
		int xid = 0x3903F326;
		short secs = 0;
		short flags = 0;
		List<DHCPOption> options = new ArrayList<>();
		options.add(new DHCPOption((byte) 53, (byte) 1, new byte[] { (byte) DHCPProtocol.DHCP_OP_DISCOVER }));
		options.add(new DHCPOption((byte) 55, (byte) 4, new byte[] { (byte) 1, (byte) 3, (byte) 6, (byte) 15 }));
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, DHCPProtocol.DHCP_SERVER_PORT, message.getBytes());
		
		Jns3Logger.clearLog();
		dhcpProtocol.socket.receiveDatagram(datagram);
		dhcpProtocol.onTick();
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.size());
		assertEquals("[INFO] DiscoverHandler: Received DHCP Discover message"
				+ "", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}
	
}
