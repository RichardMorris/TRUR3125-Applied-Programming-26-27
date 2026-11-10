package org.singsurf.jns3.network_layer;

import java.util.Arrays;

import org.singsurf.jns3.AbstractAddress;

public class IPAddress extends AbstractAddress {
    public static IPAddress ZERO_IP = new IPAddress(new byte[] {0, 0, 0, 0});
    
    public IPAddress(byte[] bytes) {
        super(4, bytes);
    }

    public IPAddress(int[] ubytes) {
        super(4, ubytes);
    }
    public IPAddress(String addr) {
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
