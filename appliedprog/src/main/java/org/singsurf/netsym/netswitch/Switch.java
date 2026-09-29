package org.singsurf.netsym.netswitch;

import org.singsurf.netsym.Device;

/**
 * A simplified version of a switch. 
 * Each switch can choose between 256 devices according to 
 */
public class Switch implements Device {
    
    int n_bitmask_bits;
    Device[] myDevices;

    public Switch(int n_bitmask_bits) {
        this.n_bitmask_bits = n_bitmask_bits;
        myDevices = new Device[256];
    }

    /** */
}
