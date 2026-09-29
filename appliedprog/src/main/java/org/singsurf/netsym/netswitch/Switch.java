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
        switch(n_bitmask_bits) {
            case 0: case 8: case 16: case 24:
                break;
            default:
                throw new IllegalArgumentException("n_bitmask_bits should be 0, 8, 16, 24");
        }
        myDevices = new Device[256];
        if(n_bitmask_bits==0) {
            if(addr.getUnsignedByte(0) != 0)
                throw new IllegalArgumentException("Address should start 0.*.*.*");
        }
        if(n_bitmask_bits<=8) {
            if(addr.getUnsignedByte(1) != 0)
                throw new IllegalArgumentException("Address should be of form  *.0.*.*.*");
        }
        if(n_bitmask_bits<=16) {
            if(addr.getUnsignedByte(2) != 0)
                throw new IllegalArgumentException("Address should be of form  *.0.*.*.*");
        }
        if(n_bitmask_bits<=24) {
            if(addr.getUnsignedByte(3) != 0)
                throw new IllegalArgumentException("Address should be of form  *.0.*.*.*");
        }

    }

    @Override
    public void receive(Message msg) {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }

    /** */
}
