package org.singsurf.jns3.transport_layer.dhcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.hardware.DeviceFactory;
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
	void message_creation_with_options_toString() {
		var mac = DeviceFactory.getInstance().createMACAddress();
		var shaddr = DeviceFactory.getInstance().createMACAddress();
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
		options.add(new DHCPOption(DHCPMessage.OPT_MSG_TYPE, (byte) 1, new byte[] { (byte) DHCPMessage.DHCP_OP_DISCOVER }));
		options.add(new DHCPOption(DHCPMessage.OPT_SERVER_IP, (byte) 4, siaddr.getBytes()));

		byte[] macBytes = new byte[16];
		System.arraycopy(shaddr.getBytes(), 0, macBytes, 0, 6);
		options.add(new DHCPOption(DHCPMessage.OPT_SERVER_MAC, (byte) 14, macBytes));
		
		var message = new DHCPMessage(op, xid, secs, flags, addresses, options);
		assertNotNull(message);
		var str = message.toString();
		
		var expectedStr = "DHCPMessage[op=1, htype=1, hlen=6, hops=0, xid=0x3903F326, secs=0, flags=0, "
				+ "ciaddr=" + ciaddr + ", yiaddr=" + yiaddr + ", siaddr=" + siaddr + ", giaddr=" + giaddr + ", "
				+ "chaddr=" + mac + ", options=3]";
		assertEquals(expectedStr, str);
		var retrievedOptions = message.getOptions();
		assertEquals(3, retrievedOptions.size());
		
		var expectedOption1Str = "DHCPOption[code=53, length=1, data=1]";
		assertEquals(expectedOption1Str, retrievedOptions.get(0).toString());

		var expectedOption2Str = "DHCPOption[code=54, length=4, data="+siaddr.toString()+"]";
		assertEquals(expectedOption2Str, retrievedOptions.get(1).toString());
		
		var expectedOption3Str = "DHCPOption[code=6, length=14, data="+shaddr.toString()+"]";
		assertEquals(expectedOption3Str, retrievedOptions.get(2).toString());
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
	
	@Test
	void test_to_string() {
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
		
		String messageString = message.toString();
		System.out.println(messageString);
		assertNotNull(messageString);
		assertTrue(messageString.contains("DHCPMessage"));
		assertTrue(messageString.contains("op=" + op));
		assertTrue(messageString.contains("xid=0x3903F326"));
		assertTrue(messageString.contains("secs=" + secs));
		assertTrue(messageString.contains("flags=" + flags));
		assertTrue(messageString.contains("ciaddr=" + ciaddr));
		assertTrue(messageString.contains("yiaddr=" + yiaddr));
		assertTrue(messageString.contains("siaddr=" + siaddr));
		assertTrue(messageString.contains("giaddr=" + giaddr));
		assertTrue(messageString.contains("chaddr=" + mac));
		assertTrue(messageString.contains("options=" + options.size()));
	}
}