package org.singsurf.jns3.network_layer.icmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.hardware.PassthroughRouter;
import org.singsurf.jns3.network_layer.DefaultPacketReceiver;
import org.singsurf.jns3.network_layer.FragmentCombiner;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.physical_layer.BufferedConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestPassthroughRouter {
	DeviceFactory deviceFactory = new DeviceFactory();
	private PassthroughRouter router;
	private PC pc1;
	private PC pc2;
	private PingController pingcont;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = deviceFactory.createPC("PC1");
		pc2 = deviceFactory.createPC("PC2");
		
		var packetLogger1 = new DefaultPacketReceiver("PacketLogger1");
		var icmpHandler1 = new ICMPPacketReceiver(pc1.getName(), packetLogger1);
		pc1.setPacketReceiver(icmpHandler1);
		
		pingcont = PingController.newInstance(pc1);
		icmpHandler1.addHandler(pingcont);
		icmpHandler1.addHandler(new ICMPPacketLogger("ICMPLogger1"));
		
		var combiner1 = new FragmentCombiner(pc1,pc1.getPacketReceiver());
		pc1.clearFrameHandlers();
		pc1.addFrameHandler(pc1.getArpController());
		pc1.addFrameHandler(combiner1);

		var packetLogger2 = new DefaultPacketReceiver("PacketLogger2");
		var icmpHandler2 = new ICMPPacketReceiver(pc2.getName(), packetLogger2);
		pc2.setPacketReceiver(icmpHandler2);

		var pingreq = new PingRequestHandler(pc2.getName(),pc2.getIPAddress(), pc2);
		icmpHandler2.addHandler(pingreq);
		
		var combiner2 = new FragmentCombiner(pc2,pc2.getPacketReceiver());
		pc2.clearFrameHandlers();
		pc2.addFrameHandler(pc2.getArpController());
		pc2.addFrameHandler(combiner2);

		var routerIP = deviceFactory.createLocalIPAddress();
		router = new PassthroughRouter("Router1", routerIP);
	}

	@Test
	public void can_negotiate_arp_through_router() {
		
		var connection1 = new SimpleConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		// Act
		pc1.sendGARPRequest();
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		assertEquals(pc1.getMACAddress(), pc2.lookup(pc1.getIPAddress()));
		assertEquals(pc2.getMACAddress(), pc1.lookup(pc2.getIPAddress()));
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void can_ping_across_a_simple_network() {		
		var connection1 = new SimpleConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.sendGARPRequest();
		// Act
		Jns3Logger.clearLog();
		
		pingcont.sendPingRequest(pc2.getIPAddress(), 64);

		System.out.println(Jns3Logger.asString());
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
	public void can_ping_across_a_buffered_network() {
		var connection1 = new BufferedConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new BufferedConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();		
		pingcont.sendPingRequest(pc2.getIPAddress(), 64);
		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals("PC2", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received ping request from "+pc1.getIPAddress()));
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(1, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(2, Jns3Logger.size());
		var item1 = Jns3Logger.getItem(1);
		assertEquals("PC1", item1.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item1.level());
		assertTrue(item1.message().startsWith("Received ping reply from "+pc2.getIPAddress()));
		assertTrue(item1.message().endsWith("RTT: 4 ticks"));
		assertTrue(Jns3Logger.isErrorFree());
	}

	
	@Test
	public void normal_traffic_passes_through() {
		var connection1 = new BufferedConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new BufferedConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		
		var packet = pc1.createIPPacket(IPPacket.TCP,pc2.getIPAddress(), 
				new byte[] {1,2,3,4,5,6,7,8,9,10});
		pc1.sendPacket(packet);

		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals("PacketLogger2", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item0.level());
		assertTrue(item0.message().startsWith("Received packet"));
	}

	@Test
	public void normal_with_ttl_1_is_dropped() {
		var connection1 = new BufferedConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new BufferedConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		
		var packet = pc1.createIPPacket(IPPacket.TCP,pc2.getIPAddress(), 
				new byte[] {1,2,3,4,5,6,7,8,9,10});
		packet.setTimeToLive(1);
		assertTrue(packet.isValidChecksum());
		pc1.sendPacket(packet);

		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals("Router1", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.INFO, item0.level());
		assertTrue(item0.message().startsWith("Dropped packet"));
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(1, Jns3Logger.size());
	}

	@Test
	public void ping_with_ttl_1_is_replies_with_error() {
		var connection1 = new BufferedConnection(pc1.getEndPoint(), router.getEndPoint(0));
		var connection2 = new BufferedConnection(pc2.getEndPoint(), router.getEndPoint(1));
		assertNotNull(connection1);
		assertNotNull(connection2);
		
		pc1.sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		
		pingcont.sendPingRequest(pc2.getIPAddress(), 1);
		assertEquals(0, Jns3Logger.size());
		// tick
		GlobalWallClock.advanceBy(1);
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals("Router1", item0.deviceName());
		assertEquals(Jns3Logger.LogLevel.INFO, item0.level());
		assertTrue(item0.message().startsWith("Expired ping"));
		// tick
		GlobalWallClock.advanceBy(1);
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.size());
	}

}
