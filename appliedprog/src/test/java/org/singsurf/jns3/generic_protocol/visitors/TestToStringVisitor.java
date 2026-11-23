package org.singsurf.jns3.generic_protocol.visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.application_layer.ChatApplication;
import org.singsurf.jns3.application_layer.http.HTTPClient;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.tcp.TCPSegment;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

class TestToStringVisitor {

	ToStringVisitor visitor;
	PC pc1;
	PC pc2;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = DeviceFactory.getInstance().createPC("PC1");
		pc2 = DeviceFactory.getInstance().createPC("PC2");
		visitor = new ToStringVisitor(pc1);
	}

	@Test
	void test_visit_EthernetFrame() {
		// arrange
        MACAddress destination = new MACAddress("00:11:22:33:44:55");
        MACAddress source = new MACAddress("66:77:88:99:AA:BB");

        byte[] payload = new byte[] {
            0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x45, 0x74, 0x68, 0x65, 0x72, 0x6E, 0x65, 0x74, 0x21
        };
        EthernetFrame frame = new EthernetFrame(destination, source, (short) payload.length, payload);

        // act
        String result = visitor.toString(frame);
        
        // assert
        System.out.println(result);
        String expected = frame.toHeaderString() + "\n  " + frame.getPayloadAsString() + "\n";
        assertEquals(expected, result);
	}

	@Test
	void test_visit_EthernetFrame_with_ARP() {
		// arrange

        ARPMessage arpMessage = new ARPMessage(true,
        	pc1.getMACAddress(), pc1.getIPAddress(), 
        	pc2.getMACAddress(), pc2.getIPAddress()); 
        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		ARPProtocol.ARP_ETHER_TYPE, arpMessage.getBytes());
        
        // act
        String result = visitor.toString(frame);
        System.out.println(result);
        
        // assert
        String expected = frame.toHeaderString()
        		+ "\n  " + arpMessage.toString() + "\n";
        assertEquals(expected, result);
	}

	@Test
	void test_visit_EthernetFrame_with_IP() {
		// arrange
		String content = "Hello IP";
        byte[] data = content.getBytes();
        IPPacket packet = new IPPacket(34, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), data); 

        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		IPProtocol.ETHERTYPE, packet.getBytes());
        
        // act
        String result = visitor.toString(frame);
        System.out.println(result);
        
        // assert
        String expected = frame.toHeaderString()
        		+ "\n  " + packet.toHeaderString()
        		+ "\n    " + content + "\n";
        assertEquals(expected, result);
	}

	@Test
	void test_visit_IP_packet() {
		// arrange
		String content = "Hello IP";
        byte[] data = content.getBytes();
        IPPacket packet = new IPPacket(34, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), data); 
        
        // act
        String result = visitor.toString(packet);
        System.out.println(result);
        
        // assert
        String expected = packet.toHeaderString()
        		+ "\n  " + content + "\n";
        assertEquals(expected, result);
	}
	
	@Test
	void icmp_ping() {
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
		String result = visitor.toString(pingRequest);
		System.out.println(result);
		String expected = pingRequest.toString() + "\n";
		assertEquals(expected, result);
	}

	@Test
	void ttl_exceeded_for_icmp_ping_in_packet() {
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
        IPPacket packet = new IPPacket(IPPacket.IMCP, 64,
                pc1.getIPAddress(), pc2.getIPAddress(), pingRequest.getBytes()); 
		
		var errorMsg = ICMPMessage.createTTLExpired(packet);
		var errorPacket = new IPPacket(
			IPPacket.IMCP, 64,
			pc2.getIPAddress(), packet.getSourceAddress(), 
			errorMsg.getBytes());

		
		String result = visitor.toString(errorPacket);
		System.out.println(result);
		StringBuilder expected = new StringBuilder();
		expected.append(errorPacket.toHeaderString()).append('\n');
		expected.append("  ").append(errorMsg.toHeaderString()).append('\n');
		expected.append("    ").append(packet.toHeaderString()).append('\n');
		expected.append("      ").append(pingRequest.toString()).append('\n');
		System.out.println(expected);
		assertEquals(expected.toString(), result);
	}

	@Test
	void ttl_exceeded_for_dhcp() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
        IPPacket packet = new IPPacket(IPPacket.UDP, 64,
                pc1.getIPAddress(), pc2.getIPAddress(), datagram.getBytes()); 
		
		var errorMsg = ICMPMessage.createTTLExpired(packet);
		var errorPacket = new IPPacket(
			IPPacket.IMCP, 64,
			pc2.getIPAddress(), packet.getSourceAddress(), 
			errorMsg.getBytes());

		
		String result = visitor.toString(errorPacket);
		System.out.println(result);
		StringBuilder expected = new StringBuilder();
		expected.append(errorPacket.toHeaderString()).append('\n');
		expected.append("  ").append(errorMsg.toHeaderString()).append('\n');
		expected.append("    ").append(packet.toHeaderString()).append('\n');
		expected.append("      ").append(datagram.toHeaderString()).append('\n');
		assertEquals(expected.toString(), result);
	}

	@Test
	void packet_with_icmp_ping() {
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
        IPPacket packet = new IPPacket(IPPacket.IMCP, 64,
                pc1.getIPAddress(), pc2.getIPAddress(), pingRequest.getBytes()); 

		String result = visitor.toString(packet);
		System.out.println(result);
		String expected = 
				packet.toHeaderString()
				+ "\n  " + pingRequest.toString() + "\n";
		assertEquals(expected, result);
	}

	@Test
	void frame_with_icmp_ping() {
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
        IPPacket packet = new IPPacket(IPPacket.IMCP, 64,
                pc1.getIPAddress(), pc2.getIPAddress(), pingRequest.getBytes()); 
        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		IPProtocol.ETHERTYPE, packet.getBytes());

        String result = visitor.toString(frame);
		System.out.println(result);
		String expected = 
				frame.toHeaderString() 
				+ "\n  " + packet.toHeaderString()
				+ "\n    " + pingRequest.toString() + "\n";
		assertEquals(expected, result);
	}
	
	
	@Test
	void udp_datagram() {
		short srcPort = 1234;
		short destPort = 5678;
		String content = "Hello UDP";
        byte[] data = content.getBytes();
		var udpDatagram = new UDPDatagram(srcPort, destPort, data);

		String result = visitor.toString(udpDatagram);
		System.out.println(result);
		String expected = udpDatagram.toHeaderString() + "\n"
				+ "  " + content + "\n";
		assertEquals(expected, result);
	}

	@Test
	void packet_with_udp_datagram() {
		short srcPort = 1234;
		short destPort = 5678;
		String content = "Hello UDP";
        byte[] data = content.getBytes();
		var datagram = new UDPDatagram(srcPort, destPort, data);
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);

		String result = visitor.toString(packet);
		System.out.println(result);
		String expected = 
				packet.toHeaderString() + "\n"
				+ "  " + datagram.toHeaderString() + "\n"
				+ "    " + content + "\n";
		assertEquals(expected, result);
	}

	@Test
	void frame_with_udp_datagram() {
		short srcPort = 1234;
		short destPort = 5678;
		String content = "Hello UDP";
        byte[] data = content.getBytes();
		var udpDatagram = new UDPDatagram(srcPort, destPort, data);
        IPPacket packet = udpDatagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
        		
        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		IPProtocol.ETHERTYPE, packet.getBytes());

        String result = visitor.toString(frame);
		System.out.println(result);
		String expected = 
				frame.toHeaderString() + "\n"
				+ "  " + packet.toHeaderString() + "\n"
				+ "    " + udpDatagram.toHeaderString() + "\n"
				+ "      " + content + "\n";
		assertEquals(expected, result);
	}

	@Test
	void dhcp_discover() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(pc1.getMACAddress(), pc1.getIPAddress());
		String result = visitor.toString(dhcpDiscover);
		System.out.println(result);
		StringBuilder expected = new StringBuilder(dhcpDiscover.toHeaderString() + "\n");
		var options = dhcpDiscover.getOptions();
		expected.append("  ").append(options.get(0)).append("\n");
		expected.append("  ").append(options.get(1)).append("\n");
		assertEquals(expected.toString(), result);
	}

	@Test
	void dhcp_discover_in_datagram() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		
		String result = visitor.toString(datagram);
		System.out.println(result);
		StringBuilder expected = new StringBuilder();
		expected.append(datagram.toHeaderString()).append("\n");
		expected.append("  ").append(dhcpDiscover.toHeaderString() + "\n");
		var options = dhcpDiscover.getOptions();
		expected.append("    ").append(options.get(0)).append("\n");
		expected.append("    ").append(options.get(1)).append("\n");
		assertEquals(expected.toString(), result);
	}

	@Test
	void dhcp_discover_in_packet() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
		
		String result = visitor.toString(packet);
		System.out.println(result);
		StringBuilder expected = new StringBuilder();
		expected.append(packet.toHeaderString()).append("\n");
		expected.append("  ").append(datagram.toHeaderString()).append("\n");
		expected.append("    ").append(dhcpDiscover.toHeaderString() + "\n");
		var options = dhcpDiscover.getOptions();
		expected.append("      ").append(options.get(0)).append("\n");
		expected.append("      ").append(options.get(1)).append("\n");
		assertEquals(expected.toString(), result);
	}

	@Test
	void dhcp_discover_in_frame() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		IPProtocol.ETHERTYPE, packet.getBytes());
		
		String result = visitor.toString(frame);
		System.out.println(result);
		StringBuilder expected = new StringBuilder();
		expected.append(frame.toHeaderString()).append("\n");
		expected.append("  ").append(packet.toHeaderString()).append("\n");
		expected.append("    ").append(datagram.toHeaderString()).append("\n");
		expected.append("      ").append(dhcpDiscover.toHeaderString() + "\n");
		var options = dhcpDiscover.getOptions();
		expected.append("        ").append(options.get(0)).append("\n");
		expected.append("        ").append(options.get(1)).append("\n");
		assertEquals(expected.toString(), result);
	}

	@Test
	void chat_massage_in_udp_datagram() {
		var datagram = new UDPDatagram(ChatApplication.PRIVATE_PORT, 
				ChatApplication.PUBLIC_PORT, "Chat hello".getBytes());

		String result = visitor.toString(datagram);
		System.out.println(result);
		String expected = datagram.toHeaderString() + "\n"
				+ "  Chat hello\n";
		assertEquals(expected, result);
	}

	@Test
	void tcp_syn_in_packet() {
		short srcPort = 1234;
		short destPort = 5678;
		int initialSeqNum=87654321;
		int initialRemoteSeqNum=0;
		
		var pseudoHeader = new TCPSegment.PseudoHeader(
				pc1.getIPAddress(), pc2.getIPAddress(), IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN, 
				(short) 2048,
				new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		
		String result = visitor.toString(packet);
		System.out.println(result);	
		
		String expected = packet.toHeaderString() + "\n"
				+ "  " + snycDatagram.toHeaderString() + "\n";
		assertEquals(expected, result);
	}

	@Test
	void http_req_syn_in_packet() {
		var http1 = new HTTPClient(pc1); 
		String request = http1.getRequestString("/index.html");
		
		short srcPort = 1234;
		short destPort = 80;
		int initialSeqNum=87654321;
		int initialRemoteSeqNum=0;
		
		var pseudoHeader = new TCPSegment.PseudoHeader(
				pc1.getIPAddress(), pc2.getIPAddress(), IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN,
				(short) 2048,
				request.getBytes(),
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		
		String result = visitor.toString(packet);
		System.out.println("["+result+"]");
		var lines = request.split("\n");
		
		StringBuilder expected = new StringBuilder();
		expected.append(packet.toHeaderString()).append("\n");
		expected.append("  ").append(snycDatagram.toHeaderString()).append("\n");
		for(String line: lines) {
			expected.append("    ").append(line).append("\n");
		}
				
		assertEquals(expected.toString(), result);
	}

}
