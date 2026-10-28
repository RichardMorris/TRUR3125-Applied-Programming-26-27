package org.singsurf.jns3.network_layer.icmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.DefaultPacketReceiver;
import org.singsurf.jns3.network_layer.IPFragmentHandler;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestICMPHandler {
	DeviceFactory deviceFactory = new DeviceFactory();
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void can_log_an_imcp_message() {
		PC pc = deviceFactory.createPC("PC1");

		var packetLogger = new DefaultPacketReceiver("PacketLogger");
		var icmpHandler = new ICMPPacketReceiver(pc.getName(), packetLogger);

		icmpHandler.addHandler(new ICMPMessageLogger("ICMPLogger"));
		
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x34, (byte) 0x12, (byte) 0x78, (byte) 0x56 };
		var senderIP = deviceFactory.createLocalIPAddress();
		var destinationIP = deviceFactory.createLocalIPAddress();

		var packet = new IPPacket(IPPacket.IMCP, 64, senderIP, destinationIP, data);

		Jns3Logger.clearLog();
		icmpHandler.receive(packet);
		assertEquals(1, Jns3Logger.size());
		assertEquals("ICMPLogger", Jns3Logger.getItem(0).deviceName());
		
		var tcpPacket = new IPPacket(IPPacket.TCP, 64, senderIP, destinationIP, data);
		Jns3Logger.clearLog();
		icmpHandler.receive(tcpPacket);
		assertEquals(1, Jns3Logger.size());
		assertEquals("PacketLogger", Jns3Logger.getItem(0).deviceName());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void pc_can_log_an_imcp_message() {
		PC pc = deviceFactory.createPC("PC1");

		// set packet handlers using chain of responsibility pattern
		// ICMPPacketReceiver will handle ICMP packets and pass others to packetLogger
		var packetLogger = new DefaultPacketReceiver("PacketLogger");
		var icmpHandler = new ICMPPacketReceiver(pc.getName(), packetLogger);

		icmpHandler.addHandler(new ICMPMessageLogger("ICMPLogger"));
		
		pc.setPacketReceiver(icmpHandler);
		var combiner = new IPFragmentHandler(pc,pc.getPacketReceiver());
		pc.clearFrameHandlers();
		pc.addFrameHandler(combiner);
		
		var senderIP = deviceFactory.createLocalIPAddress();
		var destinationIP = pc.getIPAddress();
		var senderMAC = deviceFactory.createMACAddress();
		
		byte[] data = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x34, (byte) 0x12, (byte) 0x78, (byte) 0x56 };
		var packet = new IPPacket(IPPacket.IMCP, 64, senderIP, destinationIP, data);
		byte[] payload = packet.getBytes();
		var frame = new EthernetFrame(senderMAC, pc.getMACAddress(), (short) payload.length, payload);

		Jns3Logger.clearLog();
		pc.receive(frame);
		assertEquals(1, Jns3Logger.size());
		assertEquals("ICMPLogger", Jns3Logger.getItem(0).deviceName());
		
		// arrange for a TCP packet to be received and logged by the packetLogger
		var tcpPacket = new IPPacket(IPPacket.TCP, 64, senderIP, destinationIP, data);
		byte[] payload1 = tcpPacket.getBytes();
		var frame2 = new EthernetFrame(senderMAC, pc.getMACAddress(), (short) payload1.length, payload1);

		// act
		Jns3Logger.clearLog();
		pc.receive(frame2);

		// assert
		assertEquals(1, Jns3Logger.size());
		assertEquals("PacketLogger", Jns3Logger.getItem(0).deviceName());
		assertTrue(Jns3Logger.isErrorFree());
	}
	
	@Test
	public void can_ping_across_a_simple_network() {
		PC pc1 = deviceFactory.createPC("PC1");
		PC pc2 = deviceFactory.createPC("PC2");
		
		var packetLogger1 = new DefaultPacketReceiver("PacketLogger1",Jns3Logger.LogLevel.ERROR);
		var icmpHandler1 = new ICMPPacketReceiver(pc1.getName(), packetLogger1);
		pc1.setPacketReceiver(icmpHandler1);
		
		var pingcont = PingController.newInstance(pc1);
		icmpHandler1.addHandler(pingcont);
		icmpHandler1.addHandler(new ICMPMessageLogger("ICMPLogger1"));
		
		var combiner1 = new IPFragmentHandler(pc1,pc1.getPacketReceiver());
		pc1.clearFrameHandlers();
		pc1.addFrameHandler(pc1.getArpController());
		pc1.addFrameHandler(combiner1);

		var packetLogger2 = new DefaultPacketReceiver("PacketLogger2",Jns3Logger.LogLevel.ERROR);
		var icmpHandler2 = new ICMPPacketReceiver(pc2.getName(), packetLogger2);
		pc2.setPacketReceiver(icmpHandler2);

		var pingreq = new PingRequestHandler(pc2, pc2);
		icmpHandler2.addHandler(pingreq);
		
		var combiner2 = new IPFragmentHandler(pc2,pc2.getPacketReceiver());
		pc2.clearFrameHandlers();
		pc2.addFrameHandler(pc2.getArpController());
		pc2.addFrameHandler(combiner2);
		
		var connection = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		
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

}
