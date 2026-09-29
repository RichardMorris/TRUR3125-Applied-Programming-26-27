package org.singsurf.netsym.netswitch;

/**
 * A simplified version of a switch. 
 * Each switch can choose between 256 devices according to 
 */
public class Switch extends IPDevice {
    
    int n_bitmask_bits;
    IPDevice[] myDevices = new IPDevice[256];

    public Switch(IPAddr addr, int n_bitmask_bits) {
        super(addr);
        myDevices[0] = this;
        this.n_bitmask_bits = n_bitmask_bits;
        switch(n_bitmask_bits) {
            case 0: case 8: case 16: case 24:
                break;
            default:
                throw new IllegalArgumentException("n_bitmask_bits should be 0, 8, 16, 24");
        }
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

    public void addDevice(IPDevice dev) {
        var devaddr = dev.getAddr();

        var index = switch(n_bitmask_bits) {
            case 0 -> devaddr.getUnsignedByte(0);
            case 8 -> devaddr.getUnsignedByte(1);
            case 16 -> devaddr.getUnsignedByte(2);
            case 24 -> devaddr.getUnsignedByte(3);
            default ->
                throw new IllegalStateException("bad n_bitmask_bits");
        };
        if(index==0) {
                throw new IllegalArgumentException("Address of device "+devaddr+" does not match address of switch "+getAddr());
        }
        switch (n_bitmask_bits) {
            case 24:
                if(getAddr().getUnsignedByte(2) != devaddr.getUnsignedByte(2))
                    throw new IllegalArgumentException("Address of device "+devaddr+" does not match address of switch "+getAddr());
            case 16:
                if(getAddr().getUnsignedByte(1) != devaddr.getUnsignedByte(1))
                    throw new IllegalArgumentException("Address of device "+devaddr+" does not match address of switch "+getAddr());
            case 8:
                if(getAddr().getUnsignedByte(0) != devaddr.getUnsignedByte(0))
                    throw new IllegalArgumentException("Address of device "+devaddr+" does not match address of switch "+getAddr());
            default:
                break;
        }

        myDevices[index] = dev;
    }

    public IPDevice getDevice(int index) {
        return myDevices[index];
    }

    @Override
    public void receive(Message msg) {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }

    /** */
}
