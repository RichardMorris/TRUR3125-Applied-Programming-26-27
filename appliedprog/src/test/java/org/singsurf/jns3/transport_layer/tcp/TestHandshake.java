package org.singsurf.jns3.transport_layer.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.http.HTTPClient;
import org.singsurf.jns3.application_layer.http.HTTPServer;
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
	private HTTPServer http2;
	
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
		http2 = new HTTPServer(pc2);
		tcp2.register(http2);
		
		var connection = new BufferedConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		
		pc1.getArpProtocol().sendGARPRequest();
		GlobalWallClock.advanceBy(2);
		
		
	}

	@Test
	void test_handshake_stage1() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertTrue(tcp1.messageQueue.isEmpty());
		assertEquals(1, tcp1.clientViews.size());
		
		var ent = tcp1.clientViews.entrySet().iterator().next();
		assertEquals(pc2.getIPAddress(), ent.getKey().ip());
		assertEquals(80, ent.getKey().port());
		var cv = ent.getValue();
		
		System.out.println("SYN "+cv.getInitialSeqNum());
		assertEquals(0, cv.getInitialRemoteSeqNum());
		var text = new String(cv.getMessage().getContents());
		assertTrue(text.startsWith("GET /index.html HTTP/0.1"));

		GlobalWallClock.advanceBy(1);

		var ent2 = tcp2.serverViews.entrySet().iterator().next();
		assertEquals(pc1.getIPAddress(), ent2.getKey().ip());
		assertEquals(12345, ent2.getKey().port());
		var sv = ent2.getValue();
		
		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());		
		System.out.println("PC2 SYN " + sv.getInitialSeqNum());
	}

	@Test
	void test_handshake_stage2() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertTrue(tcp1.messageQueue.isEmpty());
		
		GlobalWallClock.advanceBy(2);

		var cv = tcp1.clientViews.entrySet().iterator().next().getValue();
		var sv = tcp2.serverViews.entrySet().iterator().next().getValue();
		assertNotNull(cv);
		assertNotNull(sv);
		
		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());
		assertEquals(cv.getInitialRemoteSeqNum(), sv.getInitialSeqNum());
		assertEquals(cv.getCurrentSeqNum(), sv.getCurrentRemoteSeqNum());
		
		assertEquals(cv.getInitialSeqNum()+ 1, cv.getCurrentSeqNum());
	}

	@Test
	void test_handshake_stage3() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertTrue(tcp1.messageQueue.isEmpty());
		
		GlobalWallClock.advanceBy(3);

		var cv = tcp1.clientViews.entrySet().iterator().next().getValue();
		var sv = tcp2.serverViews.entrySet().iterator().next().getValue();
		assertNotNull(cv);
		assertNotNull(sv);
		
		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());
		assertEquals(cv.getInitialRemoteSeqNum(), sv.getInitialSeqNum());
		
		assertEquals(cv.getCurrentSeqNum(), sv.getCurrentRemoteSeqNum());
		assertEquals(cv.getCurrentRemoteSeqNum(), sv.getCurrentSeqNum());
		
		assertEquals(cv.getInitialSeqNum()+ 1, cv.getCurrentSeqNum());
		assertEquals(sv.getInitialSeqNum()+ 1, sv.getCurrentSeqNum());
	}

	@Test
	void http_request_receive_by_server() {
		Jns3Logger.clearLog();
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertTrue(tcp1.messageQueue.isEmpty());
		
		GlobalWallClock.advanceBy(4);

		Jns3Logger.println();
		
		var cv = tcp1.clientViews.entrySet().iterator().next().getValue();
		var sv = tcp2.serverViews.entrySet().iterator().next().getValue();
		assertNotNull(cv);
		assertNotNull(sv);
		
		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());
		assertEquals(cv.getInitialRemoteSeqNum(), sv.getInitialSeqNum());
		
		assertEquals(cv.getCurrentSeqNum(), sv.getCurrentRemoteSeqNum());
		assertEquals(cv.getCurrentRemoteSeqNum(), sv.getCurrentSeqNum());
		
		String request = http1.getRequestString("/index.html");

		assertEquals(cv.getInitialSeqNum()+ 1 + request.length(), cv.getCurrentSeqNum());
		assertEquals(sv.getInitialSeqNum()+ 1, sv.getCurrentSeqNum());
		
		assertEquals(request, http2.lastRequest);
		var item = Jns3Logger.getItem(1);
		assertEquals("PC2-HTTPD", item.deviceName());
		assertEquals(Jns3Logger.LogLevel.RECEIVED, item.level());
		assertEquals("http::"+ pc1.getIPAddress() + ":12345->" + pc2.getIPAddress() + ":80", item.message());
		System.out.println("Request received by server:\n[" + http2.lastRequest + "]");
	}

	
	
	@AfterEach
	void tearDown() throws Exception {
		GlobalWallClock.getInstance().clear();
		var errorFree = Jns3Logger.isErrorFree();
		Jns3Logger.clearLog();
		assertTrue(errorFree);
	}

}
