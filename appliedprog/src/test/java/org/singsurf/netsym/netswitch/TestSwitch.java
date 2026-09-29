package org.singsurf.netsym.netswitch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestSwitch {
    @Test 
    public void create_bit_mask_0() {
        IPAddr addr = new IPAddr("0.0.0.0");
        Switch sw = new Switch(addr, 0);
        assertNotNull(sw);
    }

    @Test 
    public void create_bad_bit_mask_0() {
        IPAddr addr = new IPAddr("138.0.0.0");
        assertThrows(IllegalArgumentException.class, ()->
            new Switch(addr, 0));
    }

    @Test 
    public void create_bit_mask_8() {
        IPAddr addr = new IPAddr("138.0.0.0");
        Switch sw = new Switch(addr, 8);
        assertNotNull(sw);
    }

    @Test 
    public void create_bad_bit_mask_8() {
        IPAddr addr = new IPAddr("138.39.0.0");
        assertThrows(IllegalArgumentException.class, ()->
            new Switch(addr, 8));
    }

    @Test 
    public void create_bit_mask_16() {
        IPAddr addr = new IPAddr("138.39.0.0");
        Switch sw = new Switch(addr, 16);
        assertNotNull(sw);
    }

    @Test 
    public void create_bad_bit_mask_16() {
        IPAddr addr = new IPAddr("138.39.2.0");
        assertThrows(IllegalArgumentException.class, ()->
            new Switch(addr, 16));
    }

    @Test 
    public void create_bit_mask_24() {
        IPAddr addr = new IPAddr("138.253.39.0");
        Switch sw = new Switch(addr, 24);
        assertNotNull(sw);
    }

    @Test 
    public void create_bad_bit_mask_24() {
        IPAddr addr = new IPAddr("138.253.39.2");
        assertThrows(IllegalArgumentException.class, ()->
            new Switch(addr, 24));
    }

    @Test 
    public void create_bad_bit_mask_9() {
        IPAddr addr = new IPAddr("138.0.0.0");
        assertThrows(IllegalArgumentException.class, ()->
            new Switch(addr, 9));
    }

}
