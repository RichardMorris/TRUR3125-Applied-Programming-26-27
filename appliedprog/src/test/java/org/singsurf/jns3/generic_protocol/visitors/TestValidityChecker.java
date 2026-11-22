package org.singsurf.jns3.generic_protocol.visitors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.application_layer.ChatApplication;
import org.singsurf.jns3.application_layer.http.HTTPClient;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.arp.ARPMessage;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;
import org.singsurf.jns3.network_layer.PacketFragmentor;
import org.singsurf.jns3.network_layer.icmp.ICMPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPMessage;
import org.singsurf.jns3.transport_layer.dhcp.DHCPProtocol;
import org.singsurf.jns3.transport_layer.tcp.TCPSegment;
import org.singsurf.jns3.transport_layer.udp.UDPDatagram;

class TestValidityChecker {

	ValidityChecker visitor;
	ToStringVisitor dumper;
	PC pc1;
	PC pc2;
	
	@BeforeEach
	void setUp() throws Exception {
		pc1 = DeviceFactory.getInstance().createPC("PC1");
		pc2 = DeviceFactory.getInstance().createPC("PC2");
		visitor = new ValidityChecker(pc1);
		dumper = new ToStringVisitor(pc1);
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
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
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
        // act
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}

	@Test
	void test_visit_EthernetFrame_with_IP() {
		// arrange
		String content = "Hello IP";
        byte[] data = content.getBytes();
        IPPacket packet = new IPPacket(1234, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), data); 

        EthernetFrame frame = new EthernetFrame(
        		pc2.getMACAddress(), pc1.getMACAddress(),
        		IPProtocol.ETHERTYPE, packet.getBytes());
        
        // act
        // act
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}

	@Test
	void test_visit_IP_packet() {
		// arrange
		String content = "Hello IP";
        byte[] data = content.getBytes();
        IPPacket packet = new IPPacket(1234, 64,
            pc1.getIPAddress(), pc2.getIPAddress(), data); 
        
        // act
        // act
        boolean result = visitor.check(packet);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}
	
	@Test
	void icmp_ping() {
		var message = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
        // act
        boolean result = visitor.check(message);
        
        // assert
        assertTrue(result);
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

		
        // act
        boolean result = visitor.check(errorPacket);
        
        // assert
        assertTrue(result, "Packet should be valid");
        
        var extractedPacket = errorMsg.dataAsIPHeader();
        assertTrue(visitor.check(extractedPacket));
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

		
        // act
        boolean result = visitor.check(errorPacket);
        
        // assert
        assertTrue(result);
        
        var extractedPacket = errorMsg.dataAsIPHeader();
        assertFalse(visitor.check(extractedPacket));

	}

	@Test
	void packet_with_icmp_ping() {
		var pingRequest = new ICMPMessage(ICMPMessage.PING_REQUEST_TYPE, 
				(byte) 0, (short) 0x1234, (short) 0x5678);
        IPPacket packet = new IPPacket(IPPacket.IMCP, 64,
                pc1.getIPAddress(), pc2.getIPAddress(), pingRequest.getBytes()); 

        // act
        boolean result = visitor.check(packet);
        
        // assert
        assertTrue(result, "Packet should be valid");
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

        // act
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}
	
	
	@Test
	void udp_datagram() {
		short srcPort = 1234;
		short destPort = 5678;
		String content = "Hello UDP";
        byte[] data = content.getBytes();
        var psuedo = new UDPDatagram.PseudoHeader(pc1.getIPAddress(), pc2.getIPAddress(), 
        		IPPacket.UDP, (short) (data.length + 8));
		var datagram = new UDPDatagram(srcPort, destPort, data, psuedo);

        // act
        boolean result = visitor.check(datagram);
        
        // assert
        assertTrue(result, "Datagram should be valid");
	}

	@Test
	void packet_with_udp_datagram() {
		short srcPort = 1234;
		short destPort = 5678;
		String content = "Hello UDP";
        byte[] data = content.getBytes();
		var datagram = new UDPDatagram(srcPort, destPort, data);
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);

        // act
        boolean result = visitor.check(packet);
        
        // assert
        assertTrue(result, "Packet should be valid");
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

        // act
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}

	@Test
	void dhcp_discover() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(pc1.getMACAddress(), pc1.getIPAddress());
        // act
        boolean result = visitor.check(dhcpDiscover);
        
        // assert
        assertTrue(result);
	}

	@Test
	void dhcp_discover_in_datagram() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
        var psuedo = new UDPDatagram.PseudoHeader(pc1.getIPAddress(), pc2.getIPAddress(), 
        		IPPacket.UDP, (short) (dhcpDiscover.getBytes().length + 8));
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes(), psuedo);
		
        // act
        boolean result = visitor.check(datagram);
        
        // assert
        assertTrue(result, "Datagram should be valid");
	}

	@Test
	void dhcp_discover_in_packet() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
		
        // act
        boolean result = visitor.check(packet);
        
        // assert
        assertTrue(result, "Packet should be valid");
	}

	@Test
	void dhcp_discover_in_packet_with_altered_address_fails_at_transport_level() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
        assertTrue(visitor.check(packet), "Packet and DG should be valid");
        // act
		packet.setSourceIP(new IPAddress("138.253.39.2"));

		// assert
        assertTrue(visitor.check(packet, ValidityChecker.Level.NETWORK), "Packet should be valid");
        assertFalse(visitor.check(packet,ValidityChecker.Level.TRANSPORT), "Packet should be valid, but not the datagram");
        assertFalse(visitor.check(packet), "Packet should not be valid at ALL level");
	}

	@Test
	void dhcp_discover_in_packet_with_drectemented_tll_should_pass() {
		var dhcpDiscover = DHCPMessage.createDiscoverMessage(
				pc1.getMACAddress(), pc1.getIPAddress());
		var datagram = new UDPDatagram(DHCPProtocol.DHCP_CLIENT_PORT, 
				DHCPProtocol.DHCP_SERVER_PORT, 
				dhcpDiscover.getBytes());
		IPPacket packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
        assertTrue(visitor.check(packet), "Packet and DG should be valid");
        // act
		packet.decrementTTL();
        
        // assert
        assertTrue(visitor.check(packet, ValidityChecker.Level.NETWORK), "Packet should be valid");
        assertTrue(visitor.check(packet,ValidityChecker.Level.TRANSPORT), "Packet should be valid, but not the datagram");
        assertTrue(visitor.check(packet), "Packet should not be valid at ALL level");
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
		
        // act
        boolean result = visitor.check(frame);
        
        // assert
        assertTrue(result, "Ethernet frame should be valid");
	}

	@Test
	void chat_massage_in_udp_datagram() {
		String content = "Chat hello";
        var psuedo = new UDPDatagram.PseudoHeader(pc1.getIPAddress(), pc2.getIPAddress(),
        		IPPacket.UDP, (short) (content.length() + 8));
		var datagram = new UDPDatagram(ChatApplication.PRIVATE_PORT, 
				ChatApplication.PUBLIC_PORT, content.getBytes(), psuedo);

        // act
        boolean result = visitor.check(datagram);
        
        // assert
        assertTrue(result, "Datagram should be valid");
	}

	@Test
	void large_chat_massage_in_packet() {
		var largeMessage = Jns3Logger.generateOnesComplementTable();
        var psuedo = new UDPDatagram.PseudoHeader(pc1.getIPAddress(), pc2.getIPAddress(), 
        		IPPacket.UDP, (short) (largeMessage.length() + 8));
		var datagram = new UDPDatagram(ChatApplication.PRIVATE_PORT, 
				ChatApplication.PUBLIC_PORT, largeMessage.getBytes(), psuedo);
		var packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
        // act
        boolean result = visitor.check(packet);
        
        // assert
        assertTrue(result);
	}

	@Test
	void for_large_chat_massage_in_fragments_first_fragment_is_valid_udp_remaininder_not() {
		var largeMessage = Jns3Logger.generateOnesComplementTable();
        var psuedo = new UDPDatagram.PseudoHeader(pc1.getIPAddress(), pc2.getIPAddress(), 
        		IPPacket.UDP, (short) (largeMessage.length() + 8));
		var datagram = new UDPDatagram(ChatApplication.PRIVATE_PORT, 
				ChatApplication.PUBLIC_PORT, largeMessage.getBytes(), psuedo);
		var packet = datagram.toIPPacket(pc1.getIPAddress(), pc2.getIPAddress(), 64);
		PacketFragmentor packetFragmentor = new PacketFragmentor();

        // act
		var fragments = packetFragmentor.getFragments(packet);
		assertFalse(packet.isFragment());
		System.out.println("Fragments: " + fragments.size());
		int count = 0;
		for(IPPacket fragment : fragments) {
			assertTrue(visitor.check(fragment,ValidityChecker.Level.NETWORK), "Fragment should be valid");
			var result = visitor.check(fragment,ValidityChecker.Level.TRANSPORT);
			System.out.println("Fragment " + count + " is valid at transport level: " + result 
					+ " is fragment " + fragment.isFragment());
			assertFalse(result);
			assertTrue(fragment.isFragment());
		}
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
				TCPSegment.FLAG_SYN, new byte[0], 
				pseudoHeader);
		var packet = snycDatagram.toPacket();
		
		boolean result = visitor.check(packet);
		assertTrue(result);
	}

	@Test
	void tcp_syn() {
		short srcPort = 1234;
		short destPort = 5678;
		int initialSeqNum=87654321;
		int initialRemoteSeqNum=0;
		
		var pseudoHeader = new TCPSegment.PseudoHeader(
				pc1.getIPAddress(), pc2.getIPAddress(), IPPacket.TCP, (short) 20);
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN, new byte[0], 
				pseudoHeader);
		
		boolean result = visitor.check(snycDatagram);
		assertTrue(result);
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
				pc1.getIPAddress(), pc2.getIPAddress(), IPPacket.TCP, (short) (20 + request.length()));
		var snycDatagram = new TCPSegment(
				srcPort, destPort,
				initialSeqNum, initialRemoteSeqNum, 
				TCPSegment.FLAG_SYN, request.getBytes(),
				pseudoHeader);
		assertTrue(snycDatagram.isValid());

		var packet = snycDatagram.toPacket();
		
		boolean result = visitor.check(packet);
		assertTrue(result);
	}


}
