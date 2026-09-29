package org.singsurf.netsym.netswitch;

/**
 * A simplified version of a switch. 
 * Each switch can choose between 256 devices according to 
 */
public class Switch extends IPDevice {
    
    int n_bitmask_bits;
    Device[] myDevices;

    public Switch(IPAddr addr, int n_bitmask_bits) {
        super(addr);
        this.n_bitmask_bits = n_bitmask_bits;
        myDevices = new Device[256];
    }

    @Override
    public void receive(Message msg) {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }

    /** */
}
