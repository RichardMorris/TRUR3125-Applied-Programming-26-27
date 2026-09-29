package org.singsurf.netsym.netswitch;

import java.util.Arrays;

public class IPAddr {
    final byte[] bytes;

    public IPAddr(byte[] bytes) {
        if(bytes.length!=4)
            throw new IllegalArgumentException("Expected a four byte array found"+Arrays.toString(bytes));
        this.bytes = bytes;
    }

    
    IPAddr(Byte[] bytes) {
        this.bytes = new byte[4];
        if(bytes.length!=4)
            throw new IllegalArgumentException("Expected a four byte array found"+Arrays.toString(bytes));
        this.bytes[0] = bytes[0];
        this.bytes[1] = bytes[1];
        this.bytes[2] = bytes[2];
        this.bytes[3] = bytes[3];
    }

    public IPAddr(String addr) {
        this(
            Arrays.stream(addr.split("\\."))
            .map( s ->  (byte) Integer.parseInt(s))
            .toArray(Byte[]::new));
    }

    public byte[] getBytes() {
        return bytes.clone(); // safty ensure calling code cannot modify
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bytes[0] & 0xFF);
        sb.append('.');
        sb.append(bytes[1] & 0xFF);
        sb.append('.');
        sb.append(bytes[2] & 0xFF);
        sb.append('.');
        sb.append(bytes[3] & 0xFF);
        return sb.toString();
    }
}
