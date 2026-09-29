package org.singsurf.netsym.netswitch;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

public class IPAddrTest {
    @Test 
    public void create_from_four_bytes() {
        byte[] bytes = new byte[] { (byte) 138, (byte) 253, 39, 2};

        IPAddr addr1 = new IPAddr(bytes);
        byte[] bytes2 = addr1.getBytes();
        assertArrayEquals(bytes, bytes2);
        assertNotSame(bytes2, bytes);
    }

    @Test 
    public void create_from_four_ints() {
        int[] bytes = new int[] { 138, 253, 39, 2};
        byte[] expected = new byte[] { (byte) 138, (byte) 253, 39, 2};
        assertEquals(4, bytes.length);

        IPAddr addr1 = new IPAddr(bytes);
        byte[] bytes2 = addr1.getBytes();
        assertArrayEquals(expected, bytes2);
    }

    // should really test IP can't be modified

    @Test
    public void create_from_string() {
        String addr = "138.253.39.2";
        byte[] bytes = new byte[] { (byte) 138, (byte) 253, 39, 2};
        IPAddr addr1 = new IPAddr(addr);
        byte[] bytes2 = addr1.getBytes();
        assertArrayEquals(bytes, bytes2);
        
    }

    @Test 
    public void to_human_readable_string() {
        byte[] bytes = new byte[] { (byte) 138, (byte) 253, 39, 2};
        String expect = "138.253.39.2";
        IPAddr addr1 = new IPAddr(bytes);
        assertEquals(expect, addr1.toString());
    }
}
