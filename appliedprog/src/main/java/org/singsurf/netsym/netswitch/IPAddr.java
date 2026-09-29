package org.singsurf.netsym.netswitch;

import java.util.Arrays;

public class IPAddr {
    final byte[] bytes;

    public IPAddr(byte[] bytes) {
        if(bytes.length!=4)
            throw new IllegalArgumentException("Expected a four byte array found"+Arrays.toString(bytes));
        this.bytes = bytes.clone();
    }

    // Internal constructor 
    // private to prevent external use
    private IPAddr(Byte[] bytes) {
        this.bytes = new byte[4];
        if(bytes.length!=4)
            throw new IllegalArgumentException("Expected a four byte array found"+Arrays.toString(bytes));
        this.bytes[0] = bytes[0];
        this.bytes[1] = bytes[1];
        this.bytes[2] = bytes[2];
        this.bytes[3] = bytes[3];
    }

    public IPAddr(int[] ubytes) {
        this(Arrays.stream(ubytes).mapToObj(i -> (byte) i).toArray(Byte[]::new));
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

    /**
     * Returns the given byte as a number from 0 to 255
     * @param n index of the byte
     * @return an int between 0 and 255
     */
    public int getUnsignedByte(int n) {
        return bytes[n] & 0xFF;
    }

    public int[] getUnsignedBytes() {
        return new int[] {
            getUnsignedByte(0),
            getUnsignedByte(1),
            getUnsignedByte(2),
            getUnsignedByte(3)
        };
    }
    public String toString() {
        var us = getUnsignedBytes();
        return String.join(".",
            Arrays.stream(us)
            .mapToObj(i-> Integer.toString(i))
            .toList());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IPAddr other = (IPAddr) obj;
        if (!Arrays.equals(bytes, other.bytes))
            return false;
        return true;
    }

    
}
