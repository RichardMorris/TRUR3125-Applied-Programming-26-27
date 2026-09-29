package org.singsurf.netsym.netswitch;

public abstract class IPDevice implements Device {
    IPAddr addr;

    public IPDevice(IPAddr addr) {
        this.addr = addr;
    }

    public IPAddr getAddr() {
        return addr;
    }

}
