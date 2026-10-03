package org.singsurf.jns3.data_link_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class TestMACAddress {

    @Test 
    public void constructor_from_bytes() {
        var bytes = new byte[]{(byte)0x00, (byte)0x1A, (byte)0x2B, (byte)0x3C, (byte)0x4D, (byte)0x5E};
        MACAddress mac = new MACAddress(bytes);
        byte[] b = mac.getBytes();
        assertArrayEquals(bytes, b);
    }

    @Test
    public void constructor_from_ints() {
        var ints = new int[]{0x00, 0x1A, 0x2B, 0x3C, 0x4D, 0x5E};
        MACAddress mac = new MACAddress(ints);
        byte[] b = mac.getBytes();
        assertArrayEquals(new byte[]{(byte)0x00, (byte)0x1A, (byte)0x2B, (byte)0x3C, (byte)0x4D, (byte)0x5E}, b);
    }

    @Test
    public void constructor_from_string() {
        MACAddress mac = new MACAddress("00:1A:2B:3C:4D:5E");
        byte[] b = mac.getBytes();
        assertArrayEquals(new byte[]{(byte)0x00, (byte)0x1A, (byte)0x2B, (byte)0x3C, (byte)0x4D, (byte)0x5E}, b);
    }

    @Test
    public void constructor_from_string_with_dashes() {
        MACAddress mac = new MACAddress("00-1A-2B-3C-4D-5E");
        byte[] b = mac.getBytes();
        assertArrayEquals(new byte[]{(byte)0x00, (byte)0x1A, (byte)0x2B, (byte)0x3C, (byte)0x4D, (byte)0x5E}, b);
    }

    @Test
    public void testToString() {
        MACAddress mac = new MACAddress("00-1A-2B-3C-4D-5E");
        assertEquals("00:1A:2B:3C:4D:5E", mac.toString());
    }

    @Test
    public void testEquality() {
        MACAddress mac1 = new MACAddress("00:1A:2B:3C:4D:5E");
        MACAddress mac2 = new MACAddress("00-1A-2B-3C-4D-5E");
        assertEquals(mac1, mac2);
    }

    @Test
    public void testInequality() {
        MACAddress mac1 = new MACAddress("00:1A:2B:3C:4D:5E");
        MACAddress mac2 = new MACAddress("00:1A:2B:3C:4D:5F");
        assertNotEquals(mac1, mac2);
    }
}
