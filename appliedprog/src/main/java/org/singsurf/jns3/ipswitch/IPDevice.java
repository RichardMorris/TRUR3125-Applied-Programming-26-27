package org.singsurf.jns3.ipswitch;

public abstract class IPDevice implements Device {
    IPAddr addr;

    public IPDevice(IPAddr addr) {
        this.addr = addr;
    }

    public IPAddr getAddr() {
        return addr;
    }

}
