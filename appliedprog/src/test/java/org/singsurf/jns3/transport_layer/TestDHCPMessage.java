package org.singsurf.jns3.transport_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage.DHCPOption;

class TestDHCPMessage {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void message_creation_with_no_options() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ciaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var yiaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var siaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var giaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var addresses = new DHCPMessage.DHCPAddresses(mac, ciaddr, yiaddr, siaddr, giaddr);
		byte op = DHCPMessage.DHCP_OP_DISCOVER;
		int xid = 0x3903F326;
		short secs = 0;
		short flags = 0;
		List<DHCPOption> options = new ArrayList<>();
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);
		assertNotNull(message);
		assertEquals(op, message.getOp());
		assertEquals((byte) 1, message.getHtype());
		assertEquals((byte) 6, message.getHlen());
		assertEquals(xid, message.getXid());
		assertEquals(secs, message.getSecs());
		assertEquals(flags, message.getFlags());
		assertEquals(ciaddr, message.getCiaddr());
		assertEquals(yiaddr, message.getYiaddr());
		assertEquals(siaddr, message.getSiaddr());
		assertEquals(giaddr, message.getGiaddr());
		byte[] chaddr = message.getChaddr();
		for (int i = 0; i < 6; i++) {
			assertEquals(mac.getBytes()[i], chaddr[i]);
		}
		for (int i = 6; i < 16; i++) {
			assertEquals(0, chaddr[i]);
		}
		assertEquals(mac, message.getMacAddress());
		assertEquals(0, message.getOptions().size());
	}

	@Test
	void message_creation_with_options() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ciaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var yiaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var siaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var giaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var addresses = new DHCPMessage.DHCPAddresses(mac, ciaddr, yiaddr, siaddr, giaddr);
		byte op = DHCPMessage.DHCP_OP_DISCOVER;
		int xid = 0x3903F326;
		short secs = 0;
		short flags = 0;
		List<DHCPOption> options = new ArrayList<>();
		options.add(new DHCPOption((byte) 53, (byte) 1, new byte[] { (byte) DHCPMessage.DHCP_OP_DISCOVER }));
		options.add(new DHCPOption((byte) 55, (byte) 4, new byte[] { (byte) 1, (byte) 3, (byte) 6, (byte) 15 }));
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);
		assertNotNull(message);
		var retrievedOptions = message.getOptions();
		assertEquals(2, retrievedOptions.size());
		assertEquals((byte) 53, retrievedOptions.get(0).code());
		assertEquals((byte) 1, retrievedOptions.get(0).length());
		assertEquals((byte) DHCPMessage.DHCP_OP_DISCOVER, retrievedOptions.get(0).data()[0]);
		assertEquals((byte) 55, retrievedOptions.get(1).code());
		assertEquals((byte) 4, retrievedOptions.get(1).length());
		assertEquals((byte) 1, retrievedOptions.get(1).data()[0]);
		assertEquals((byte) 3, retrievedOptions.get(1).data()[1]);
		assertEquals((byte) 6, retrievedOptions.get(1).data()[2]);
		assertEquals((byte) 15, retrievedOptions.get(1).data()[3]);
		
	}
	
	@Test
	void test_to_bytes_and_from_bytes() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var ciaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var yiaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var siaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var giaddr = DeviceFactory.getInstance().createLocalIPAddress();
		var addresses = new DHCPMessage.DHCPAddresses(mac, ciaddr, yiaddr, siaddr, giaddr);
		byte op = DHCPMessage.DHCP_OP_DISCOVER;
		int xid = 0x3903F326;
		short secs = 0;
		short flags = 0;
		List<DHCPOption> options = new ArrayList<>();
		options.add(new DHCPOption((byte) 53, (byte) 1, new byte[] { (byte) DHCPMessage.DHCP_OP_DISCOVER }));
		options.add(new DHCPOption((byte) 55, (byte) 4, new byte[] { (byte) 1, (byte) 3, (byte) 6, (byte) 15 }));
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);
		
		byte[] bytes = message.getBytes();
		
		DHCPMessage parsedMessage = new DHCPMessage(bytes);
		
		assertEquals(message.getOp(), parsedMessage.getOp());
		assertEquals(message.getXid(), parsedMessage.getXid());
		assertEquals(message.getSecs(), parsedMessage.getSecs());
		assertEquals(message.getFlags(), parsedMessage.getFlags());
		assertEquals(message.getCiaddr(), parsedMessage.getCiaddr());
		assertEquals(message.getYiaddr(), parsedMessage.getYiaddr());
		assertEquals(message.getSiaddr(), parsedMessage.getSiaddr());
		assertEquals(message.getGiaddr(), parsedMessage.getGiaddr());
		assertEquals(message.getMacAddress(), parsedMessage.getMacAddress());
		assertEquals(message.getOptions().size(), parsedMessage.getOptions().size());
		for (int i = 0; i < message.getOptions().size(); i++) {
			DHCPOption originalOption = message.getOptions().get(i);
			DHCPOption parsedOption = parsedMessage.getOptions().get(i);
			assertEquals(originalOption.code(), parsedOption.code());
			assertEquals(originalOption.length(), parsedOption.length());
			assertEquals(originalOption.data().length, parsedOption.data().length);
			for (int j = 0; j < originalOption.data().length; j++) {
				assertEquals(originalOption.data()[j], parsedOption.data()[j]);
			}
		}	
	}
}
