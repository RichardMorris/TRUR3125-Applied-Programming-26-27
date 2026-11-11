package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestIPPrefix {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testWildcard() {
		IPPrefix prefix = new IPPrefix("0.0.0.0/0");
		IPAddress addr1 = new IPAddress("0.0.0.0");
		IPAddress addr2 = new IPAddress("255.255.255.255");
		IPAddress addr3 = new IPAddress("138.253.39.2");
		assertTrue(prefix.matches(addr1));
		assertTrue(prefix.matches(addr2));
		assertTrue(prefix.matches(addr3));
	}

	@Test
	void testClassA() {
		IPPrefix prefix = new IPPrefix("10.0.0.0/8");
		IPAddress addr1 = new IPAddress("10.0.0.0");
		IPAddress addr2 = new IPAddress("10.255.255.255");
		assertTrue(prefix.matches(addr1));
		assertTrue(prefix.matches(addr2));
		
		IPAddress addr3 = new IPAddress("9.255.255.255");		
		IPAddress addr4 = new IPAddress("11.0.0.0");
		assertFalse(prefix.matches(addr3));
		assertFalse(prefix.matches(addr4));
	}

	@Test
	void testClassB() {
		IPPrefix prefix = new IPPrefix("172.16.0.0/12");
		IPAddress addr1 = new IPAddress("172.16.0.0");
		IPAddress addr2 = new IPAddress("172.31.255.255");
		assertTrue(prefix.matches(addr1));
		assertTrue(prefix.matches(addr2));
		
		IPAddress addr3 = new IPAddress("172.15.255.255");		
		IPAddress addr4 = new IPAddress("172.32.0.0");
		assertFalse(prefix.matches(addr3));
		assertFalse(prefix.matches(addr4));
	}

	@Test
	void testClassC() {
		IPPrefix prefix = new IPPrefix("192.168.0.0/16");
		IPAddress addr1 = new IPAddress("192.168.0.0");
		IPAddress addr2 = new IPAddress("192.168.255.255");
		assertTrue(prefix.matches(addr1));
		assertTrue(prefix.matches(addr2));
		
		IPAddress addr3 = new IPAddress("192.167.255.255");		
		IPAddress addr4 = new IPAddress("192.169.0.0");
		assertFalse(prefix.matches(addr3));
		assertFalse(prefix.matches(addr4));
	}

	@Test
	void testSingleMachine() {
		IPPrefix prefix = new IPPrefix("192.168.1.1/32");
		IPAddress addr1 = new IPAddress("192.168.1.1");
		assertTrue(prefix.matches(addr1));
		
		IPAddress addr3 = new IPAddress("192.168.1.2");		
		IPAddress addr4 = new IPAddress("192.168.1.0");
		assertFalse(prefix.matches(addr3));
		assertFalse(prefix.matches(addr4));
	}

}
