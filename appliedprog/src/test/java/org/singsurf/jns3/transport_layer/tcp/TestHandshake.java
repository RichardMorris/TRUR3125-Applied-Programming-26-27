package org.singsurf.jns3.transport_layer.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.application_layer.http.HTTPClient;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.BufferedConnection;

class TestHandshake {
	PC pc1;
	PC pc2;
	private HTTPClient http1;
	private TCPProtocol tcp1;
	private TCPProtocol tcp2;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = DeviceFactory.getInstance().createPC("PC1");
		pc2 = DeviceFactory.getInstance().createPC("PC2");
		
		tcp1 = new TCPProtocol(pc1);
		pc1.getIPProtocol().register(tcp1);
		http1 = new HTTPClient(pc1);
		tcp1.register(http1);
		
		tcp2 = new TCPProtocol(pc2);
		pc2.getIPProtocol().register(tcp2);
		
		var connection = new BufferedConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		
		pc1.getArpProtocol().sendGARPRequest();
		GlobalWallClock.advanceBy(2);
		
		
	}

	@Test
	void test_handshake_stage1() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertTrue(tcp1.messageQueue.isEmpty());
		assertEquals(1, tcp1.waitingMessages.size());
		
		var ent = tcp1.waitingMessages.entrySet().iterator().next();
		assertEquals(pc2.getIPAddress(), ent.getKey().ip());
		assertEquals(80, ent.getKey().port());
		
		var msg = ent.getValue();
		System.out.println("SYN "+msg.seqNum());
		assertEquals(0, msg.ackNum());
		var text = new String(msg.message().getContents());
		assertTrue(text.startsWith("GET /index.html HTTP/0.1"));

		GlobalWallClock.advanceBy(1);

		var ent2 = tcp2.initialSynNums.entrySet().iterator().next();
		assertEquals(pc1.getIPAddress(), ent2.getKey().ip());
		assertEquals(12345, ent2.getKey().port());
		assertEquals(msg.seqNum(), ent2.getValue());
		
		var ent3 = tcp2.intialAckNums.entrySet().iterator().next();
		assertEquals(pc1.getIPAddress(), ent3.getKey().ip());
		assertEquals(12345, ent3.getKey().port());
		System.out.println("ACK " + ent3.getValue());
	}

}
