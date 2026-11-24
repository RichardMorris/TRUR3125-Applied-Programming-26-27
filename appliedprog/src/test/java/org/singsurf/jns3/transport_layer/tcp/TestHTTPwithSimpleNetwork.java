package org.singsurf.jns3.transport_layer.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.http.FileLoader;
import org.singsurf.jns3.application_layer.http.HTTPClient;
import org.singsurf.jns3.application_layer.http.HTTPServer;
import org.singsurf.jns3.generic_protocol.PolledSocket;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.physical_layer.BufferedConnection;

class TestHTTPwithSimpleNetwork {
	PC pc1;
	PC pc2;
	HTTPClient http1;
	TCPProtocol tcp1;
	TCPProtocol tcp2;
	HTTPServer http2;
	short windowSize = 2048;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = DeviceFactory.getInstance().createPC("PC1");
		pc2 = DeviceFactory.getInstance().createPC("PC2");
		
		tcp1 = pc1.getTCPProtocol();
		tcp1.setWindowSize(windowSize);
		pc1.getIPProtocol().register(tcp1);
		http1 = new HTTPClient(pc1);
		tcp1.register(http1);
		
		tcp2 = pc2.getTCPProtocol();
		tcp2.setWindowSize(windowSize);
		pc2.getIPProtocol().register(tcp2);
		http2 = new HTTPServer(pc2);
		tcp2.register(http2);
		http2.setFileLoader(new FileLoader() {
			@Override
			public InputStream loadFile(String path) {
				if(path.startsWith("/")) {
					path = path.substring(1);
				}
				return getClass().getResourceAsStream(path);
			}
		});
		
		var connection = new BufferedConnection(pc1.getEndPoint(), pc2.getEndPoint());
		assertNotNull(connection);
		
		pc1.getArpProtocol().sendGARPRequest();
		GlobalWallClock.advanceBy(2);
	}

	@Test
	void test_handshake_stage1() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		assertEquals(1, tcp1.channels.size());
		
		var ent = tcp1.channels.entrySet().iterator().next();
		assertEquals(pc2.getIPAddress(), ent.getKey().ip());
		assertEquals(80, ent.getKey().port());
		var cv = ent.getValue();
		
		System.out.println("SYN "+cv.getInitialSeqNum());
		assertEquals(0, cv.getInitialRemoteSeqNum());
//		var text = new String(cv.getMessage().getContents());
//		assertTrue(text.startsWith("GET /index.html HTTP/0.1"));

		GlobalWallClock.advanceBy(1);

		var ent2 = tcp2.channels.entrySet().iterator().next();
		assertEquals(pc1.getIPAddress(), ent2.getKey().ip());
		assertEquals(12345, ent2.getKey().port());
		var sv = ent2.getValue();
		
		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());		
		System.out.println("PC2 SYN " + sv.getInitialSeqNum());
	}

	@Test
	void test_handshake_stage2() {
		http1.requestPage(pc2.getIPAddress(), "/index.html");
		
		
		GlobalWallClock.advanceBy(2);

		var cv = tcp1.channels.entrySet().iterator().next().getValue();
		var sv = tcp2.channels.entrySet().iterator().next().getValue();
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
		
		
		GlobalWallClock.advanceBy(2);
		
		GlobalWallClock.advanceBy(1);

		var cv = tcp1.channels.entrySet().iterator().next().getValue();
		var sv = tcp2.channels.entrySet().iterator().next().getValue();
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
		
		
		GlobalWallClock.advanceBy(4);
		var cv = tcp1.channels.entrySet().iterator().next().getValue();
		assertNotNull(cv.currentMessage);
		GlobalWallClock.advanceBy(1);
		assertNull(cv.currentMessage);

		Jns3Logger.println();
		
		var sv = tcp2.channels.entrySet().iterator().next().getValue();
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
		assertEquals("SocketMessage["+ pc1.getIPAddress() + ":12345->" + pc2.getIPAddress() + ":80]", item.message());
		System.out.println("Request received by server:\n[" + http2.lastRequest + "]");
		
	}

	@Test
	void received_small_page() {
		PolledSocket.LOG_MESSAGES = true;
		pc1.getTerminal().quite = true;
		Jns3Logger.clearLog();
		http1.requestPage(pc2.getIPAddress(), "/small.html");
		
		
		GlobalWallClock.advanceBy(5);
		var cv = tcp1.channels.entrySet().iterator().next().getValue();
		var sv = tcp2.channels.entrySet().iterator().next().getValue();

		Jns3Logger.println();
		Jns3Logger.clearLog();
		GlobalWallClock.advanceBy(1);
		Jns3Logger.println();

		Jns3Logger.clearLog();
		GlobalWallClock.advanceBy(1);
		Jns3Logger.println();

		
		Jns3Logger.clearLog();
		GlobalWallClock.advanceBy(1);
		Jns3Logger.println();

		Jns3Logger.clearLog();
		GlobalWallClock.advanceBy(1);
		Jns3Logger.println();

		assertEquals(cv.getInitialSeqNum(), sv.getInitialRemoteSeqNum());
		assertEquals(cv.getInitialRemoteSeqNum(), sv.getInitialSeqNum());
		
		assertEquals(cv.getCurrentSeqNum(), sv.getCurrentRemoteSeqNum());
		assertEquals(cv.getCurrentRemoteSeqNum(), sv.getCurrentSeqNum());
		
		String request = http1.getRequestString("/small.html");
		String response = http2.getReplyHeader(170);

		var term = pc1.getTerminal();
		var lines = term.lines;
		for(var line : lines) {
			System.out.println("Terminal: " + line);
		}
		
		var expectedRelSeq = cv.getInitialSeqNum()+ 1 + request.length() - cv.getInitialSeqNum();
		var actualRelSeq = cv.getCurrentSeqNum() - cv.getInitialSeqNum();
		assertEquals(expectedRelSeq, actualRelSeq);
		
		
		var svExpectedRelSeq = sv.getInitialSeqNum()+ 1
				+response.length() + 170  - sv.getInitialSeqNum();
		var svCurRelSeq = sv.getCurrentSeqNum() - sv.getInitialSeqNum();
		assertEquals(svExpectedRelSeq, svCurRelSeq);
		
		var pages = http1.getReceivedPages();
		assertEquals(1, pages.size());
		var page = pages.get(0);
		assertEquals(170, page.length());
		var lines2 = page.split("\r\n");
		assertEquals("<html>", lines2[0]);
		assertEquals("<head>", lines2[1]);
		assertEquals("	<title>JNS3 Server - small page</title>", lines2[2]);
		assertEquals("</html>", lines2[9]);
		PolledSocket.LOG_MESSAGES = false;
	}

	@Test
	void can_load_index_html() throws IOException {
		var stream = http2.loadFile("/index.html");
		var bytes = stream.readAllBytes();
		assertEquals(192, bytes.length);
	}
	
	@Test
	void test_response_header() {
		var bytes = http2.getFullResponse("/index.html");
		var str = new String(bytes);
		var lines = str.split("\r\n");
		assertEquals("HTTP/0.1 200 OK", lines[0]);
		assertEquals("Server: JNS-HTTP-Server/0.1", lines[1]);
		assertEquals("Transfer-Encoding: chunked", lines[2]);
		assertEquals("Content-Type: text/html; charset=ASCII", lines[3]);
		assertEquals("Content-Length: 192", lines[4]);
		assertEquals("",lines[5]);
		assertEquals("<html>", lines[6]);
		assertEquals("<head>", lines[7]);
		assertEquals("	<title>JNS3 Server</title>", lines[8]);
		assertEquals("</head>", lines[9]);
		assertEquals("<body>", lines[10]);
		assertEquals("<h1>Welcome</h1>", lines[11]);
	}
	
	@Test
	void test_response_header_not_found() {
		var bytes = http2.getFullResponse("/notfound.html");
		var str = new String(bytes);
		var lines = str.split("\r\n");
		assertEquals("HTTP/0.1 404 Not Found", lines[0]);
		Jns3Logger.clearLog();
	}
	
	@Test
	void parse_request() {
		var request = http1.getRequestString("/index.html");
		var response = http2.parseRequest(request);
		var str = new String(response);
		var lines = str.split("\r\n");
		assertEquals("HTTP/0.1 200 OK", lines[0]);
	}
	
	@Test
	void test_before() {
		assertTrue(TCPChannel.before(2, 3, 0));
		int initial = Integer.MAX_VALUE - 5;
		assertTrue(TCPChannel.before(initial+1, initial+2, initial));
		assertTrue(TCPChannel.before(initial+1, initial+20, initial));
		assertFalse(initial+1< initial+20);
	}
	
	
	@AfterEach
	void tearDown() throws Exception {
		GlobalWallClock.getInstance().clear();
		var errorFree = Jns3Logger.isErrorFree();
		if(!errorFree) {
			Jns3Logger.println();
		}
		Jns3Logger.clearLog();
		assertTrue(errorFree);
	}

}
