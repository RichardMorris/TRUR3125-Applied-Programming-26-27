package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.ChatApplication;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.RouterEthernetProtocol;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.PacketHandler;
import org.singsurf.jns3.network_layer.icmp.ICMPProtocol;
import org.singsurf.jns3.physical_layer.CountingConnection;
import org.singsurf.jns3.physical_layer.FILOBufferedConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.udp.UDPProtocol;

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
		
		router = new HomeRouter("HomeRouter", 
				DeviceFactory.getInstance().createSequentialMACAddress(),
				new IPAddress("192.168.1.1"));

		var eth = new RouterEthernetProtocol(router);
		router.addProtocol(eth);
//		router.getPhysicalProtocol().register(eth);
		
		var arpProtocol= new ARPProtocol(router,true);
		router.addProtocol(arpProtocol);
		eth.register(arpProtocol);

		var ipProtocol = new IPProtocol(router,true);
		router.addProtocol(ipProtocol);
		eth.register(ipProtocol);

		var imcp = new ICMPProtocol(router);
		ipProtocol.register(imcp);
		router.addProtocol(imcp);
		
		var udp = new UDPProtocol(router);
		ipProtocol.register(udp);
		router.addProtocol(udp);
		
		var dhcp = new DHCPProtocol(router,true);
		udp.register(dhcp);
		router.addProtocol(dhcp);
			
		ipProtocol.addHandler(new PacketHandler() {
			@Override
			public HandlerStatus handle(IPPacket type) {
				ipProtocol.getPacketSender().send(type);
				return HandlerStatus.COMPLETED;
			}});
		eth.register(ipProtocol);
		
		eth.addHandler(new ProtocolHandler<EthernetFrame>() {
			@Override
			public HandlerStatus handle(EthernetFrame type) {
				router.getFrameSender().send(type);
				return HandlerStatus.COMPLETED;
			}
		});
		
		router.setMulticastAddress(new IPAddress("192.168.1.255"));


		
		var connection1 = new SimpleConnection(router.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new SimpleConnection(router.getEndPoint(1), pc2.getEndPoint());
		var connection3 = new SimpleConnection(router.getEndPoint(2), pc3.getEndPoint());
		var connection4 = new SimpleConnection(router.getEndPoint(3), pc4.getEndPoint());
		
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		
		ChatApplication.showPrompts = false;
	}

	@Test
	void can_send_garp_request() {
		pc1.getArpProtocol().sendGARPRequest();
		System.out.println(Jns3Logger.asString());
		assertEquals(
				pc2.getMACAddress(), 
				pc1.getArpProtocol().lookup(pc2.getIPAddress()));
		assertEquals(
				pc1.getMACAddress(), 
				pc4.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(
				router.getMACAddress(), 
				pc1.getArpProtocol().lookup(router.getIPAddress()));
		assertEquals(
				pc1.getMACAddress(), 
				router.getArpProtocol().lookup(pc1.getIPAddress()));
		
		
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
	void can_broadcast_frame() {
		pc1.getArpProtocol().sendGARPRequest();
		Jns3Logger.clearLog();
		byte[] payload = "Hello from PC1".getBytes();
		short etherType = (short) payload.length;
		var frame = new EthernetFrame(MACAddress.BROADCAST_MAC, pc1.getMACAddress(), etherType, payload);
		var fs1 = pc1.getEthernetProtocol().getFrameSender();
		fs1.send(frame);
		Jns3Logger.println();
		assertEquals(3, Jns3Logger.size());
		assertEquals("PC2", Jns3Logger.getItem(0).deviceName());
		assertEquals("PC3", Jns3Logger.getItem(1).deviceName());
		assertEquals("PC4", Jns3Logger.getItem(2).deviceName());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void can_send_packet_to_established_addresses() {
		pc1.getArpProtocol().sendGARPRequest();
		pc2.getArpProtocol().sendGARPRequest();
		Jns3Logger.clearLog();
		byte[] payload = "Hello from PC1".getBytes();
		
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), payload);
		
		var fs1 = pc1.getIPProtocol().getPacketSender();
		fs1.send(packet);
		Jns3Logger.println();
		assertEquals(1, Jns3Logger.size());
		assertEquals("PC2", Jns3Logger.getItem(0).deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, 
				Jns3Logger.getItem(0).level());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void packet_to_established_addresses_from_unknown_device() {
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		pc2.getArpProtocol().sendGARPRequest();
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));
		Jns3Logger.clearLog();
		byte[] payload = "Hello from PC1".getBytes();
		
        IPPacket packet = new IPPacket(IPPacket.TCP, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), payload);
		
		var fs1 = pc1.getIPProtocol().getPacketSender();
		fs1.send(packet);
		Jns3Logger.println();
		assertEquals(1, Jns3Logger.size());
		assertEquals("PC2", Jns3Logger.getItem(0).deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, 
				Jns3Logger.getItem(0).level());
		assertTrue(Jns3Logger.isErrorFree());
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(pc1.getMACAddress(), router.getArpProtocol().lookup(pc1.getIPAddress()));

	}

	@Test
	void catches_ttl_expired() {
		pc1.getArpProtocol().sendGARPRequest();
		pc2.getArpProtocol().sendGARPRequest();
		Jns3Logger.clearLog();
		byte[] payload = "Hello from PC1".getBytes();
		
        IPPacket packet = new IPPacket(IPPacket.TCP, 1,
            pc1.getIPAddress(), pc2.getIPAddress(), payload);
		
		var fs1 = pc1.getIPProtocol().getPacketSender();
		fs1.send(packet);
		Jns3Logger.println();
		assertEquals(3, Jns3Logger.size());
		assertEquals("PC1", Jns3Logger.getItem(2).deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, 
				Jns3Logger.getItem(2).level());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void can_ping_across_a_simple_network() throws IOException {		
		var connection1 = new SimpleConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.getArpProtocol().sendGARPRequest();
		pc2.getArpProtocol().sendGARPRequest();
		// Act
		Jns3Logger.clearLog();
		Jns3Logger.startCapture();
		pc1.getICMPProtocol().sendPingRequest(pc2.getIPAddress(), 64);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.println("Captured output:\n"+output);
		var lines = output.split("\n");
		assertEquals("PING " + pc2.getIPAddress() + " 28 bytes of data.", lines[0].trim());
		assertEquals("28 bytes from "+pc2.getIPAddress()+": icmp_seq=1 ttl=63 time=0 ticks", lines[1].trim());
		assertEquals(2, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		var item1 = Jns3Logger.getItem(1);
		assertEquals("PC2", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received ping request from "+pc1.getIPAddress()));
		assertEquals("PC1", item1.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item1.level());
		assertTrue(item1.message().startsWith("Received ping reply from "+pc2.getIPAddress()));
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void can_ping_the_router() throws IOException {		
		var connection1 = new SimpleConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.getArpProtocol().sendGARPRequest();
		pc2.getArpProtocol().sendGARPRequest();
		// Act
		Jns3Logger.clearLog();
		Jns3Logger.startCapture();
		pc1.getICMPProtocol().sendPingRequest(router.getIPAddress(), 64);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.println("Captured output:\n"+output);
		var lines = output.split("\n");
		assertEquals("PING " + router.getIPAddress() + " 28 bytes of data.", lines[0].trim());
		assertEquals("28 bytes from "+router.getIPAddress()+": icmp_seq=1 ttl=64 time=0 ticks", lines[1].trim());

		assertEquals(2, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		var item1 = Jns3Logger.getItem(1);
		assertEquals("HomeRouter", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received ping request from "+pc1.getIPAddress()));
		assertEquals("PC1", item1.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item1.level());
		assertTrue(item1.message().startsWith("Received ping reply from "+router.getIPAddress()));
		assertTrue(Jns3Logger.isErrorFree());
	}
	
	@Test
	public void traceroute_through_a_simple_network() throws IOException {		
		pc1.getArpProtocol().sendGARPRequest();
		pc2.getArpProtocol().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();		
		Jns3Logger.startCapture();
		pc1.traceroute(pc2.getIPAddress());
		String output = Jns3Logger.endCapture();
		System.out.print(output);

		var lines = output.split("\n");
		assertEquals(3, lines.length);
		assertEquals("traceroute to "+pc2.getIPAddress()+", 64 hops max", lines[0].trim());
		//		assertEquals(0, Jns3Logger.size());
		// tick
		assertEquals("1\t"+ router.getIPAddress()+ "\t0 ticks", lines[1].trim());
		assertEquals("2\t"+ pc2.getIPAddress()+ "\t0 ticks", lines[2].trim());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void discover_across_simple_network() {
		DeviceFactory.getInstance().resetSequentialLocalIPAddress();
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		System.out.println("Discover");
		System.out.println("router mac"+ router.getMACAddress());
		pc1.getDHCPProtocol().startDiscovery();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();

			Jns3Logger.clearLog();
			System.out.println("Tick " + 1);
			GlobalWallClock.getInstance().advanceTime();
			System.out.println(Jns3Logger.asString());

		DHCPMessage offerMessage = null;
		IPAddress yaddr = null;
		for(var ent : pc1.getDHCPProtocol().getAcceptedOfferMessages().entrySet()) {
				yaddr = ent.getValue().getYiaddr();
				offerMessage = ent.getValue();
				break;
		}
		assertTrue(Jns3Logger.isErrorFree());
		System.out.println("Offer message: " + offerMessage);
		assertEquals(pc1.getMACAddress(), router.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(router.getMACAddress(), pc1.getArpProtocol().lookup(router.getIPAddress()));
		
		
		Jns3Logger.clearLog();
		System.out.println("Tick " + 2);
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());

		assertEquals(yaddr, pc1.getIPAddress());
		assertEquals(pc1.getMACAddress(), router.getArpProtocol().lookup(yaddr));
		assertEquals(router.getMACAddress(), pc1.getArpProtocol().lookup(router.getIPAddress()));
		
//		assertEquals(0, routerDHCP.offerSockets.size());
		
		
		assertTrue(Jns3Logger.isErrorFree());
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

	}

	@Test
	void all_pcs_discover_across_simple_network() {
		DeviceFactory.getInstance().resetSequentialLocalIPAddress();
		pc1.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc2.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc3.setIPAddress(IPAddress.ZERO_ADDRESS);
		pc4.setIPAddress(IPAddress.ZERO_ADDRESS);
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		
		System.out.println("Discover 1");
		pc1.getDHCPProtocol().startDiscovery();
		
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		System.out.println("Tick");
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		System.out.println("Discover 2");
		pc2.getDHCPProtocol().startDiscovery();
		
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		System.out.println("Tick");
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();

		System.out.println("Discover 3");
		pc3.getDHCPProtocol().startDiscovery();
		
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		System.out.println("Tick");
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		
		System.out.println("Discover 4");
		pc4.getDHCPProtocol().startDiscovery();
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		
		System.out.println("Tick");
		GlobalWallClock.getInstance().advanceTime();
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();

		
		assertEquals("192.168.1.2", pc1.getIPAddress().toString());
		assertEquals("192.168.1.3", pc2.getIPAddress().toString());
		assertEquals("192.168.1.4", pc3.getIPAddress().toString());
		assertEquals("192.168.1.5", pc4.getIPAddress().toString());
		assertEquals(pc1.getMACAddress(), router.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(router.getMACAddress(), pc1.getArpProtocol().lookup(router.getIPAddress()));
		
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));

		assertEquals(pc4.getMACAddress(), pc2.getArpProtocol().lookup(pc4.getIPAddress()));
		
		
		assertTrue(Jns3Logger.isErrorFree());
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

	}

	@Test
	void ipconfig_after_discover() throws IOException {
		DeviceFactory.getInstance().resetSequentialLocalIPAddress();
		Jns3Logger.clearLog();
		pc1.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(2);
		assertTrue(Jns3Logger.isErrorFree());
		Jns3Logger.clearLog();
		Jns3Logger.startCapture();
		pc1.ipconfig();
		var output = Jns3Logger.endCapture();
		System.out.println(output);
		var lines = output.split("\n");
		assertEquals(4, lines.length);
		assertEquals("Ethernet adapter PC1-IP:", lines[0].trim());
		var expected = "   IPv4 Address. . . . . . . . . . . : 192.168.1.2\n";
		assertEquals(expected.trim(), lines[2].trim());
		assertTrue(lines[2].startsWith(expected.substring(0, 30)));
		var expected2 = "   Subnet Mask . . . . . . . . . . . : 255.255.255.0";
		assertEquals(expected2.trim(), lines[3].trim());
		assertTrue(lines[3].startsWith(expected2.substring(0, 30)));
	}
	
	@Test
	public void large_messages_in_parts_through_fifo_connection() throws IOException {
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

		DeviceFactory.getInstance().resetSequentialLocalIPAddress();
		var connection1 = new FILOBufferedConnection(router.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new CountingConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);

		pc1.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		assertTrue(Jns3Logger.isErrorFree());

		assertEquals("192.168.1.2", pc1.getIPAddress().toString());
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));

		pc2.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		assertTrue(Jns3Logger.isErrorFree());

		connection2.reset();

		assertEquals("192.168.1.2", pc1.getIPAddress().toString());
		assertEquals("192.168.1.3", pc2.getIPAddress().toString());
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(pc2.getMACAddress(), pc1.getArpProtocol().lookup(pc2.getIPAddress()));

		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " of " + lines[0].length() +" chars" );
		int fragmentSize = 100;
		int startIndex = 0;
		Jns3Logger.startCapture();
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    var messagePart = fragmentBuilder.toString();
		    System.out.println("Sending message part: " + messagePart.length() + " chars");
			chat1.wisper(messagePart);
		    connection2.reset();
		    startIndex = endIndex;
		}
		System.out.println("Tick");
		GlobalWallClock.advanceBy(1);
	    System.out.println("Sent " + connection2.getForwardCount() + " frames, " + connection2.getReverseCount() + " frames received");
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		System.out.println(output.length() + " chars");
		assertEquals(5914, output.length());
		var outputLines = output.split("\n");
		assertEquals("0x00FF 255", outputLines[17].trim());
		assertEquals("0x0091 145", outputLines[19].trim());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void large_messages_through_fifo_connection() throws IOException {
		System.out.println(router);
		System.out.println(pc1);
		System.out.println(pc2);
		System.out.println(pc3);
		System.out.println(pc4);

		DeviceFactory.getInstance().resetSequentialLocalIPAddress();
		var connection1 = new FILOBufferedConnection(router.getEndPoint(0), pc1.getEndPoint());
		var connection2 = new CountingConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);

		pc1.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		assertTrue(Jns3Logger.isErrorFree());

		assertEquals("192.168.1.2", pc1.getIPAddress().toString());
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));

		pc2.getDHCPProtocol().startDiscovery();
		GlobalWallClock.advanceBy(6);
		assertTrue(Jns3Logger.isErrorFree());

		connection2.reset();

		assertEquals("192.168.1.2", pc1.getIPAddress().toString());
		assertEquals("192.168.1.3", pc2.getIPAddress().toString());
		assertEquals(pc1.getMACAddress(), pc2.getArpProtocol().lookup(pc1.getIPAddress()));
		assertEquals(pc2.getMACAddress(), pc1.getArpProtocol().lookup(pc2.getIPAddress()));

		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		connection2.reset();
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		Jns3Logger.startCapture();
		chat1.wisper(dataString);
		
		System.out.println("Tick");
		GlobalWallClock.advanceBy(1);
	    System.out.println("Sent " + connection2.getForwardCount() + " frames, " + connection2.getReverseCount() + " frames received");
	    
	    var output = Jns3Logger.endCapture();
//		System.out.print(output);
		System.out.println(output.length() + " chars");
		assertEquals(5701, output.length());
		var outputLines = output.split("\n");
		System.out.println("Output lines: " + outputLines.length);
		assertEquals("0xFF00 -255", outputLines[1].trim());
		assertEquals("0x00FF 255", outputLines[511].trim());
		assertTrue(Jns3Logger.isErrorFree());
	}

}
