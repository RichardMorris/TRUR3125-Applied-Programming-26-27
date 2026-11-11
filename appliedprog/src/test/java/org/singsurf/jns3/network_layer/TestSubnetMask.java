package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSubnetMask {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testMatches() {
		SubnetMask mask = new SubnetMask("255.255.255.0");
		IPAddress router = new IPAddress("192.168.1.1");
		IPAddress host1 = new IPAddress("192.168.1.2");
		IPAddress host2 = new IPAddress("192.168.1.254");
		
		assertTrue(mask.matches(router, router));		
		assertTrue(mask.matches(router, host1));
		assertTrue(mask.matches(router, host2));
		
		IPAddress externalHost = new IPAddress("192.168.2.1");
		assertFalse(mask.matches(router, externalHost));
	}

	
	@Test
	void testTinyMatches() {
		SubnetMask mask = new SubnetMask("255.255.255.192");
		IPAddress router = new IPAddress("192.168.1.1");
		IPAddress host1 = new IPAddress("192.168.1.2");
		IPAddress host2 = new IPAddress("192.168.1.63");
		IPAddress host3 = new IPAddress("192.168.1.254");
		
		assertTrue(mask.matches(router, router));		
		assertTrue(mask.matches(router, host1));
		assertTrue(mask.matches(router, host2));
		
		assertFalse(mask.matches(router, host3));
	}

	@Test
	void testLargeMatches() {
		SubnetMask mask = new SubnetMask("255.255.0.0");
		IPAddress router = new IPAddress("192.168.1.1");
		IPAddress host1 = new IPAddress("192.168.1.2");
		IPAddress host2 = new IPAddress("192.168.2.63");
		IPAddress host3 = new IPAddress("192.168.255.255");
		IPAddress externalHost = new IPAddress("168.253.39.2");
		
		assertTrue(mask.matches(router, router));		
		assertTrue(mask.matches(router, host1));
		assertTrue(mask.matches(router, host2));
		assertTrue(mask.matches(router, host3));
		assertFalse(mask.matches(router, externalHost));
	}

}
