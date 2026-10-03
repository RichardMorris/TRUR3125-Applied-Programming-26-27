package org.singsurf.jns3.ipswitch;

import java.util.Arrays;

import org.singsurf.jns3.AbstractAddress;

public class IPAddr extends AbstractAddress {

    public IPAddr(byte[] bytes) {
        super(4, bytes);
    }

    public IPAddr(int[] ubytes) {
        super(4, ubytes);
    }
    public IPAddr(String addr) {
        super(4,
            Arrays.stream(addr.split("\\."))
            .map( s ->  (byte) Integer.parseInt(s))
            .toArray(Byte[]::new));
    }

    public String toString() {
        var us = getUnsignedBytes();
        return String.join(".",
            Arrays.stream(us)
            .mapToObj(i-> Integer.toString(i))
            .toList());
    }
}
