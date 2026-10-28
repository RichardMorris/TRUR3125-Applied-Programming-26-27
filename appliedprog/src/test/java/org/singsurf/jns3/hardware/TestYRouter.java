package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.network_layer.DefaultPacketReceiver;
import org.singsurf.jns3.network_layer.IPFragmentHandler;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.physical_layer.BufferedConnection;
import org.singsurf.jns3.physical_layer.CountingConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;

class TestYRouter {

	DeviceFactory deviceFactory = new DeviceFactory();
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test_basic_operation() {
		PC pc1 = deviceFactory.createPC("PC1");
		PC pc2 = deviceFactory.createPC("PC2");
		PC pc3 = deviceFactory.createPC("PC3");
		
		var router = new YRouter("YRouter", 3, 5);
		
		var legConnection = new SimpleConnection(router.getEndPoint(YRouter.LEG_PORT), 
				pc1.getEndPoint());
		var leftConnection = new SimpleConnection(router.getEndPoint(YRouter.LEFT_PORT), 
				pc2.getEndPoint());
		var rightConnection = new SimpleConnection(router.getEndPoint(YRouter.RIGHT_PORT), 
				pc3.getEndPoint());
		
		assertNotNull(leftConnection);
		assertNotNull(rightConnection);
		assertNotNull(legConnection);
				
		Jns3Logger.clearLog();
		for(int i=0; i<16; i++) {
			byte[] payload = ("" + i).getBytes();
			short etherType = (short) payload.length;
			EthernetFrame frame = new EthernetFrame(MACAddress.BROADCAST_MAC, pc1.getMACAddress(), etherType, payload);
			pc1.sendFrame(frame);
		}
		for(int i=0; i<3;++i) {
			assertEquals("PC2", Jns3Logger.getItem(i).deviceName());
		}
		for(int i=3; i<8;++i) {
			assertEquals("PC3", Jns3Logger.getItem(i).deviceName());
		}
		for(int i=8; i<11;++i) {
			assertEquals("PC2", Jns3Logger.getItem(i).deviceName());
		}
		for(int i=11; i<16;++i) {
			assertEquals("PC3", Jns3Logger.getItem(i).deviceName());
		}
		
	}

	@Test
	void split_path_with_buffer_connection() {
		PC pc1 = deviceFactory.createPC("PC1");
		PC pc2 = deviceFactory.createPC("PC2");
		var router1 = new YRouter("YRouter", 3, 5);
		var router2 = new YRouter("YRouter2", 1, 1);
				
		var leg1 = new SimpleConnection(
				pc1.getEndPoint(),
				router1.getEndPoint(YRouter.LEG_PORT));

		var branch1 = new SimpleConnection(
				router1.getEndPoint(YRouter.LEFT_PORT), 
				router2.getEndPoint(YRouter.LEFT_PORT));
		var branch2 = new BufferedConnection(
				router1.getEndPoint(YRouter.RIGHT_PORT), 
				router2.getEndPoint(YRouter.RIGHT_PORT));

		var leg2 = new SimpleConnection(
				pc2.getEndPoint(),
				router2.getEndPoint(YRouter.LEG_PORT));
		
		assertNotNull(leg1);
		assertNotNull(branch1);
		assertNotNull(branch2);
		assertNotNull(leg2);
				
		Jns3Logger.clearLog();
		for(int i=0; i<16; i++) {
			byte[] payload = ("" + i).getBytes();
			short etherType = (short) payload.length;
			EthernetFrame frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
			pc1.sendFrame(frame);
		}
		
		assertEquals(6, Jns3Logger.size());
		for(int i=0; i<3;++i) {
			assertEquals(""+i, Jns3Logger.getItem(i).message());
		}
		for(int i=3; i<6;++i) {
			assertEquals(""+(i+5), Jns3Logger.getItem(i).message());
		}

		Jns3Logger.clearLog();
		
		GlobalWallClock.getInstance().advanceTime();

		assertEquals(10, Jns3Logger.size());
		for(int i=0; i<5;++i) {
			assertEquals(""+(i+3), Jns3Logger.getItem(i).message());
		}
		for(int i=5; i<10;++i) {
			assertEquals(""+(i+6), Jns3Logger.getItem(i).message());
		}
		
	}

	@Test
	public void a_large_packet_can_be_transmission_over_a_split_network() {
		// Arrange
		
		PC pc = deviceFactory.createPC("Receiver PC");
		var receiver = new DefaultPacketReceiver(pc.getName());
		var combiner = new IPFragmentHandler(pc,receiver);
		pc.clearFrameHandlers(); // Need set our own frame handlers to protect against later changes.
		pc.addFrameHandler(pc.getArpController());
		pc.addFrameHandler(combiner);

		
		PC pc2 = deviceFactory.createPC("Sender PC");
		
		var router1 = new YRouter("YRouter", 3, 5);
		var router2 = new YRouter("YRouter2", 3, 5);
		
		var leg1 = new CountingConnection(
				pc2.getEndPoint(),
				router1.getEndPoint(YRouter.LEG_PORT));

		var branch1 = new SimpleConnection(
				router1.getEndPoint(YRouter.LEFT_PORT), 
				router2.getEndPoint(YRouter.LEFT_PORT));
		var branch2 = new BufferedConnection(
				router1.getEndPoint(YRouter.RIGHT_PORT), 
				router2.getEndPoint(YRouter.RIGHT_PORT));

		var leg2 = new CountingConnection(
				router2.getEndPoint(YRouter.LEG_PORT),
				pc.getEndPoint());
		
		assertNotNull(leg1);
		assertNotNull(branch1);
		assertNotNull(branch2);
		assertNotNull(leg2);

		System.out.println("Sending GARP request");
		pc.sendGARPRequest();
		
		System.out.println("Leg 1 forward "+ leg1.getForwardCount() + " reversed " + leg1.getReverseCount());
		System.out.println("Leg 2 forward "+ leg2.getForwardCount() + " reversed " + leg2.getReverseCount());
		System.out.println("Advancing time");
		leg1.reset();
		leg2.reset();
		GlobalWallClock.getInstance().advanceTime();
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		IPPacket packet1 = new IPPacket((int) IPPacket.TCP, 64,
		pc2.getIPAddress(), pc.getIPAddress(), dataString.getBytes());
		
		// Act
		var packet = packet1;
		pc2.sendPacket(packet);
		
		System.out.println("Leg 1 forward "+ leg1.getForwardCount() + " reversed " + leg1.getReverseCount());
		System.out.println("Leg 2 forward "+ leg2.getForwardCount() + " reversed " + leg2.getReverseCount());
		System.out.println("Advancing time");
		leg1.reset();
		leg2.reset();
		GlobalWallClock.getInstance().advanceTime();
		System.out.println("Leg 1 forward "+ leg1.getForwardCount() + " reversed " + leg1.getReverseCount());
		System.out.println("Leg 2 forward "+ leg2.getForwardCount() + " reversed " + leg2.getReverseCount());

		
		// Assert
        assertEquals(1, receiver.size());
        var receivedPacket = receiver.get(0);
        assertEquals(pc2.getIPAddress(), receivedPacket.getSourceAddress());
        assertEquals(pc.getIPAddress(), receivedPacket.getDestinationAddress());
        assertEquals(IPPacket.TCP, receivedPacket.getProtocol());
        assertArrayEquals(packet.getContents(), receivedPacket.getContents());
        System.out.println(receivedPacket.getDataAsString());
        assertTrue(Jns3Logger.isErrorFree());

	}

}
