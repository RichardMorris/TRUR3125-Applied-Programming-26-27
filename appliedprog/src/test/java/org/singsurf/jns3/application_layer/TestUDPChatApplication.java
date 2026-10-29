package org.singsurf.jns3.application_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.hardware.Switch;
import org.singsurf.jns3.physical_layer.BufferedConnection;
import org.singsurf.jns3.physical_layer.CorruptedConnection;
import org.singsurf.jns3.physical_layer.CountingConnection;
import org.singsurf.jns3.physical_layer.LossyConnection;
import org.singsurf.jns3.physical_layer.SimpleConnection;
import org.singsurf.jns3.physical_layer.ThrottledConnection;

class TestUDPChatApplication {
	DeviceFactory deviceFactory = new DeviceFactory();
	private PC pc1;
	private PC pc2;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = deviceFactory.createPC("PC1");
		pc2 = deviceFactory.createPC("PC2");
	}

	@Test
	public void chat_through_switch() throws IOException {
		var pc3 = deviceFactory.createPC("PC3");
		var sw = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var connection1 = new SimpleConnection(pc1.getEndPoint(), sw.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw.getEndPoint(1));
		var connection3 = new SimpleConnection(pc3.getEndPoint(), sw.getEndPoint(2));
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		pc1.setIPAddress(deviceFactory.createSequentialLocalIPAddress());
		pc2.setIPAddress(deviceFactory.createSequentialLocalIPAddress());
		pc3.setIPAddress(deviceFactory.createSequentialLocalIPAddress());

		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc3.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		var chat3 = new ChatApplication(pc3,pc1.getIPAddress());
		// Act
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.print(output);
		assertEquals("PC3: " + pc1.getIPAddress() + " says: Hello from PC1", output.trim());
		assertTrue(Jns3Logger.isErrorFree());
		
		chat3.say("Hello from PC3");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc3.getIPAddress() + " says: Hello from PC3", output.trim());
		
		chat2.say("Time for tea");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.print(output);
		var lines = output.trim().split("\n");
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Time for tea", lines[0].trim());
		assertEquals("PC3: " + pc2.getIPAddress() + " says: Time for tea", lines[1].trim());
		
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void private_chat_through_chained_switchs() throws IOException {
		var pc3 = deviceFactory.createPC("PC3");
		var sw1 = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var sw2 = DeviceFactory.getInstance().createSwitch("Switch2", 4);
		var connection1 = new SimpleConnection(pc1.getEndPoint(), sw1.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw1.getEndPoint(1));
		var connection3 = new SimpleConnection(pc3.getEndPoint(), sw2.getEndPoint(0));
		var connection4 = new BufferedConnection(sw1.getEndPoint(2), sw2.getEndPoint(2));
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc3.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		var chat3 = new ChatApplication(pc3,pc1.getIPAddress());
		// Act
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC3: " + pc1.getIPAddress() + " says: Hello from PC1", output.trim());
		assertTrue(Jns3Logger.isErrorFree());
		
		chat3.say("Hello from PC3");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc3.getIPAddress() + " says: Hello from PC3", output.trim());
		
		chat2.say("Time for tea");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		var lines = output.trim().split("\n");
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Time for tea", lines[0].trim());
		assertEquals("PC3: " + pc2.getIPAddress() + " says: Time for tea", lines[1].trim());
		
		assertTrue(Jns3Logger.isErrorFree());
	}


	@Test
	public void chat_through_chained_switchs() throws IOException {
		var sw1 = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var sw2 = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var connection1 = new SimpleConnection(pc1.getEndPoint(), sw1.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw2.getEndPoint(1));
		var connection3 = new BufferedConnection(sw1.getEndPoint(1), sw2.getEndPoint(0));
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1);
		var chat2 = new ChatApplication(pc2);
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC2: " + pc1.getIPAddress() + " says: Hello from PC1", output.trim());
		
		chat2.say("Hello from PC2");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Hello from PC2", output.trim());
		
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void private_chat_through_simple_connection() throws IOException {
		var connection1 = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC2: " + pc1.getIPAddress() + " says: Hello from PC1", output.trim());
		
		chat2.say("Hello from PC2");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Hello from PC2", output.trim());
		
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void public_and_private_chat_through_simple_connection() throws IOException {
		var connection1 = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.print(output);
		assertEquals("", output.trim());
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals(
			new LogItem("PC2-UDPController", 
				Jns3Logger.LogLevel.ERROR, 
				"No socket found for IP address "+ pc1.getIPAddress()),
			item0);
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		
		chat2.say("Hello from PC2");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Hello from PC2", output.trim());

		chat1.sayPublic("Hello all from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC2: " + pc1.getIPAddress() + " says: Hello all from PC1", output.trim());

		
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void public_and_private_chat_through_a_simple_connection() throws IOException {
		var connection1 = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("", output.trim());
		assertEquals(1, Jns3Logger.size());
		var item0 = Jns3Logger.getItem(0);
		assertEquals(
			new LogItem("PC2-UDPController", 
				Jns3Logger.LogLevel.ERROR, 
				"No socket found for IP address "+ pc1.getIPAddress()),
			item0);
		System.out.println(Jns3Logger.asString());
		Jns3Logger.clearLog();
		
		chat2.say("Hello from PC2");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Hello from PC2", output.trim());

		chat1.sayPublic("Hello all from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC2: " + pc1.getIPAddress() + " says: Hello all from PC1", output.trim());

		
		assertTrue(Jns3Logger.isErrorFree());
	}
	
	@Test
	public void removing_host_from_private_chat() throws IOException {
		var pc3 = deviceFactory.createPC("PC3");
		var pc4 = deviceFactory.createPC("PC4");
		var sw1 = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var sw2 = DeviceFactory.getInstance().createSwitch("Switch2", 4);
		var connection1 = new SimpleConnection(pc1.getEndPoint(), sw1.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw1.getEndPoint(1));
		var connection3 = new SimpleConnection(pc3.getEndPoint(), sw2.getEndPoint(0));
		var connection4 = new SimpleConnection(pc4.getEndPoint(), sw2.getEndPoint(1));
		var connection5 = new BufferedConnection(sw1.getEndPoint(2), sw2.getEndPoint(2));
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		assertNotNull(connection5);
		
		pc1.setIPAddress(deviceFactory.createSequentialLocalIPAddress());
		pc2.setIPAddress(deviceFactory.createSequentialLocalIPAddress());
		pc3.setIPAddress(deviceFactory.createSequentialLocalIPAddress());
		pc4.setIPAddress(deviceFactory.createSequentialLocalIPAddress());

		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc3.getIPAddress());
		chat1.addHost(pc4.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		var chat3 = new ChatApplication(pc3,pc1.getIPAddress());
		var chat4 = new ChatApplication(pc4,pc1.getIPAddress());
		chat1.removeHost(pc3.getIPAddress());
		assertNotNull(chat1);
		assertNotNull(chat2);
		assertNotNull(chat3);
		assertNotNull(chat4);
		// Act
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(1);
		var output = Jns3Logger.endCapture();
		System.out.println(Jns3Logger.asString());
		System.out.print(output);
		var lines = output.trim().split("\n");
		assertEquals("PC4: " + pc1.getIPAddress() + " says: Hello from PC1", lines[0].trim());
		
		assertTrue(Jns3Logger.isErrorFree());
	}


	@Test
	public void large_messages_through_simple_connection() throws IOException {
		var connection1 = new CountingConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		connection1.reset();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " line len " + lines[0].length() );
		int fragmentSize = 50;
		int startIndex = 0;
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    chat1.wisper(fragmentBuilder.toString());
		    System.out.println("Sent " + connection1.getForwardCount() + " frames, " + connection1.getReverseCount() + " frames received");
		    startIndex = endIndex;
		}
		
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output.length() + " chars");
		assertTrue(output.length() > 5500 && output.length() < 6100);
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void large_messages_through_lossy_connection() throws IOException {
		var connection1 = new LossyConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " line len " + lines[0].length() + " num " + (500/lines[0].length()));
		int fragmentSize = 50;
		int startIndex = 0;
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    chat1.wisper(fragmentBuilder.toString());
//		    System.out.println("Sent " + connection1.getForwardCount() + " frames, " + connection1.getReverseCount() + " frames received");
		    startIndex = endIndex;
		}
		
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output.length() + " chars");
		assertTrue(output.length() > 3000 && output.length() < 3100);
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void large_messages_through_corrupted_connection() throws IOException {
		var connection1 = new CorruptedConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " line len " + lines[0].length() + " num " + (500/lines[0].length()));
		int fragmentSize = 50;
		int startIndex = 0;
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    chat1.wisper(fragmentBuilder.toString());
//		    System.out.println("Sent " + connection1.getForwardCount() + " frames, " + connection1.getReverseCount() + " frames received");
		    startIndex = endIndex;
		}
		
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.println(output.length() + " chars");
		assertTrue(output.length() > 3000 && output.length() < 3100);
		assertFalse(Jns3Logger.isErrorFree());
		for(LogItem item : Jns3Logger.getErrorLogItems()) {
				assertEquals("PC2", item.deviceName());
				assertEquals("Invalid message: Detected invalid CRC in frame", item.message());
				System.out.println(item);
		}
	}

	@Test
	public void counting_large_messages_through_lossy_connection() throws IOException {
		var connection1 = new CountingConnection(pc1.getEndPoint(), null);
		var connection2 = new LossyConnection(null,null);
		var connection3 = new CountingConnection(null, pc2.getEndPoint());
		connection1.setEnpoint2(connection2.getVirtualEndPoint(0));
		connection2.setEnpoint1(connection1.getVirtualEndPoint(1));
		connection2.setEnpoint2(connection3.getVirtualEndPoint(0));
		connection3.setEnpoint1(connection2.getVirtualEndPoint(1));
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " line len " + lines[0].length() + " num " + (500/lines[0].length()));
		int fragmentSize = 50;
		int startIndex = 0;
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    chat1.wisper(fragmentBuilder.toString());
//		    System.out.println("Sent " + connection1.getForwardCount() + " frames, " + connection1.getReverseCount() + " frames received");
		    startIndex = endIndex;
		}
		
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
//		System.out.println(output.length() + " chars");
		System.out.println("Sent " + connection1.getForwardCount() + " frames, " + connection1.getReverseCount() + " frames received");
		System.out.println("Sent " + connection3.getForwardCount() + " frames, " + connection3.getReverseCount() + " frames received");
		assertTrue(output.length() > 3000 && output.length() < 3100);
		assertEquals(56, connection1.getForwardCount());
		assertEquals(51, connection3.getForwardCount());
		chat2.wisper("Thanks for that");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals(2, connection1.getReverseCount());
		assertEquals(2, connection3.getReverseCount());
		
		assertEquals("PC1: " + pc2.getIPAddress() + " says: Thanks for that", output.trim()); 
		assertTrue(Jns3Logger.isErrorFree());
	}

	
	@Test
	public void private_chat_through_chained_switchs_with_throttle_connection() throws IOException {
		var pc3 = deviceFactory.createPC("PC3");
		var sw1 = DeviceFactory.getInstance().createSwitch("Switch1", 4);
		var sw2 = DeviceFactory.getInstance().createSwitch("Switch2", 4);
		var connection1 = new CountingConnection(pc1.getEndPoint(), sw1.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw1.getEndPoint(1));
		var connection3 = new CountingConnection(pc3.getEndPoint(), sw2.getEndPoint(0));
		var connection4 = new ThrottledConnection(sw1.getEndPoint(2), sw2.getEndPoint(2),ThrottledConnection.CAT3);
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		assertNotNull(connection4);
		
		pc1.getArpController().sendGARPRequest();
		GlobalWallClock.advanceBy(4);
		Jns3Logger.clearLog();
		connection1.reset();
		connection3.reset();
		
		var chat1 = new ChatApplication(pc1,pc3.getIPAddress());
		var chat2 = new ChatApplication(pc2);
		var chat3 = new ChatApplication(pc3,pc1.getIPAddress());
		assertNotNull(chat1);
		assertNotNull(chat2);
		assertNotNull(chat3);
		
		String dataString = Jns3Logger.generateOnesComplementTable();
		var lines = dataString.split("\n");
		var lineLength = lines.length;
		System.out.println("Data length: " + dataString.length() + " lines: " + lines.length
				+ " line len " + lines[0].length() );
		int fragmentSize = 50;
		int startIndex = 0;
		while (startIndex < lineLength) {
		    int endIndex = Math.min(startIndex + fragmentSize, lineLength);
		    StringBuilder fragmentBuilder = new StringBuilder();
		    for (int i = startIndex; i < endIndex; i++) {
		        fragmentBuilder.append(lines[i]).append("\n");
		    }
		    chat1.wisper(fragmentBuilder.toString());
		    startIndex = endIndex;
		}

		var errors = Jns3Logger.getErrorLogItems();
		System.out.println("output size: " + 0 
		+ " count1 " + connection1.getForwardCount() + " count3 " + connection3.getReverseCount() 
		+ "  errors: " + errors.size());

		for(int i=0;i<10;i++) {
			System.out.println("tick");
			
			Jns3Logger.startCapture();
			GlobalWallClock.advanceBy(1);
			var output = Jns3Logger.endCapture();
			
			errors = Jns3Logger.getErrorLogItems();
			System.out.println("output size: " + output.length() 
			+ " count1 " + connection1.getForwardCount() + " count3 " + connection3.getReverseCount() 
			+ "  errors: " + errors.size());
			
			if(connection1.getForwardCount() == connection3.getReverseCount()) {
				break;
			}
		}
		assertEquals(connection1.getForwardCount(), connection3.getReverseCount());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void private_chat_with_no_arp() throws IOException {
		var connection1 = new SimpleConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection1);
		
//		pc1.sendGARPRequest();
//		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		assertNotNull(chat2);
		chat1.say("Hello from PC1");
		Jns3Logger.startCapture();
		GlobalWallClock.advanceBy(2);
		var output = Jns3Logger.endCapture();
		System.out.print(output);
		assertEquals("PC2: "+pc1.getIPAddress()+" says: Hello from PC1", output.trim());
		System.out.println(Jns3Logger.asString());
		assertEquals(7, Jns3Logger.size());

		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	public void private_chat_with_no_arp_through_switch_with_delay() throws IOException {
		PC pc3 = deviceFactory.createPC("PC3");
		Switch sw1 = deviceFactory.createSwitch("Switch1", 4);
		
		// Need a blocking connector here to 
		var connection1 = new BufferedConnection(pc1.getEndPoint(), sw1.getEndPoint(0));
		var connection2 = new SimpleConnection(pc2.getEndPoint(), sw1.getEndPoint(1));
		var connection3 = new BufferedConnection(pc3.getEndPoint(), sw1.getEndPoint(2));
		assertNotNull(connection1);
		assertNotNull(connection2);
		assertNotNull(connection3);
		
//		pc1.sendGARPRequest();
//		GlobalWallClock.advanceBy(4);
		// Act
		Jns3Logger.clearLog();
		var chat1 = new ChatApplication(pc1,pc2.getIPAddress());
		var chat2 = new ChatApplication(pc2,pc1.getIPAddress());
		var chat3 = new ChatApplication(pc3,pc1.getIPAddress());
		chat1.addHost(pc3.getIPAddress());
		chat2.addHost(pc3.getIPAddress());
		chat3.addHost(pc2.getIPAddress());
		
		chat1.say("Hello from PC1");
		chat1.say("Is there anybody out there?");
		
		for(int i=0;i<10;i++) {
			System.out.println("tick");
			Jns3Logger.clearLog();
			Jns3Logger.startCapture();
			GlobalWallClock.advanceBy(1);
			var output = Jns3Logger.endCapture();
			System.out.println(Jns3Logger.asString());
			System.out.print(output);
		}
		
		assertTrue(Jns3Logger.isErrorFree());
	}

}
