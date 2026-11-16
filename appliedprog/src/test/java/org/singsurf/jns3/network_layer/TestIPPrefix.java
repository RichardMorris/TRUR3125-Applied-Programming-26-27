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

	@Test
	void testBitLength1() {
		// Test prefixes from /1
		// 128.0.0.0/1 matches 128.0.0.0 to 255.255.255.255
		// 0.0.0.0/1 matches 0.0.0.0 to 127.255.255.255

		IPPrefix prefix = new IPPrefix("128.0.0.0/1");
		assertTrue(prefix.matches(new IPAddress("128.0.0.0")));
		assertTrue(prefix.matches(new IPAddress("128.0.0.1")));
		assertTrue(prefix.matches(new IPAddress("128.0.1.0")));
		assertTrue(prefix.matches(new IPAddress("128.1.0.0")));
		assertTrue(prefix.matches(new IPAddress("129.0.0.0")));
		assertTrue(prefix.matches(new IPAddress("255.255.255.255")));
		assertFalse(prefix.matches(new IPAddress("0.0.0.0")));
		assertFalse(prefix.matches(new IPAddress("127.255.255.255")));

		prefix = new IPPrefix("0.0.0.0/1");
		assertTrue(prefix.matches(new IPAddress("0.0.0.0")));
		assertTrue(prefix.matches(new IPAddress("0.0.0.1")));
		assertTrue(prefix.matches(new IPAddress("0.0.1.0")));
		assertTrue(prefix.matches(new IPAddress("0.1.0.0")));
		assertTrue(prefix.matches(new IPAddress("1.0.0.0")));
		assertTrue(prefix.matches(new IPAddress("127.255.255.255")));
		assertFalse(prefix.matches(new IPAddress("128.0.0.0")));
		assertFalse(prefix.matches(new IPAddress("255.255.255.255")));

	}

	private IPAddress flipBit(IPAddress addr, int bitIndex) {
		byte[] octets = addr.getBytes();
		int byteIndex = bitIndex / 8;
		int bitInByte = 7 - (bitIndex % 8); // MSB is bit 7
		octets[byteIndex] ^= (1 << bitInByte);
		return new IPAddress(octets);
	}
	
	@Test
	void testBitLength31() {
		// Test prefixes from /1
		// 128.0.0.0/1 matches 128.0.0.0 to 255.255.255.255
		// 0.0.0.0/1 matches 0.0.0.0 to 127.255.255.255

		IPPrefix prefix = new IPPrefix("123.45.67.88/31");
		assertTrue(prefix.matches(new IPAddress("123.45.67.88")));
		assertTrue(prefix.matches(new IPAddress("123.45.67.89")));
		assertFalse(prefix.matches(new IPAddress("123.45.67.87")));
		assertFalse(prefix.matches(new IPAddress("123.45.67.90")));
		
		IPAddress flipped = flipBit(prefix.getAddress(), 31);
		assertTrue(prefix.matches(prefix.getAddress()));
		assertTrue(prefix.matches(flipped));
		flipped = flipBit(prefix.getAddress(), 30);
		assertFalse(prefix.matches(flipped));

		prefix = new IPPrefix("123.45.67.90/31");
		assertTrue(prefix.matches(new IPAddress("123.45.67.90")));
		assertTrue(prefix.matches(new IPAddress("123.45.67.91")));
		assertFalse(prefix.matches(new IPAddress("123.45.67.89")));
		assertFalse(prefix.matches(new IPAddress("123.45.67.92")));
	}

	@Test
	void tastAllBitLengths() {
		for (int i = 1; i <= 31; i++) {
			IPPrefix prefix = new IPPrefix("123.45.67.89"+ "/" + i);
			assertTrue(prefix.matches(prefix.getAddress()));
			for(int j = i; j < 32; j++) {
				IPAddress flipped = flipBit(prefix.getAddress(), j);
				assertTrue(prefix.matches(flipped));
			}
			var flipped = flipBit(prefix.getAddress(), i - 1);
			assertFalse(prefix.matches(flipped));
		}
	}
}
