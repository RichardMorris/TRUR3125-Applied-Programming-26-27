package org.singsurf.jns3;

import java.util.Arrays;

/**
 * A Base Class for network addresses, consisting of a fixed number of bytes.
 * It has no public constructors, instead it provides protected constructors for use by subclasses
 * that specify the size of the address in bytes.
 */
public abstract class AbstractAddress {
    int size;
    final byte[] bytes;

    protected AbstractAddress(int size, byte[] bytes) {
        if(bytes.length!=size)
            throw new IllegalArgumentException("Expected a " + size + " byte array found"+Arrays.toString(bytes));
        this.bytes = bytes.clone();
        this.size = size;
    }

    // Internal constructor 
    // private to prevent external use
    protected AbstractAddress(int size, Byte[] bytes) {
        this.bytes = new byte[size];
        if(bytes.length!=size)
            throw new IllegalArgumentException("Expected a " + size + " byte array found"+Arrays.toString(bytes));
        for(int i = 0; i < size; i++) {
            this.bytes[i] = bytes[i];
        }
        this.size = size;
    }

    protected AbstractAddress(int size, int[] ubytes) {
        this(size, Arrays.stream(ubytes).mapToObj(i -> (byte) i).toArray(Byte[]::new));
    }
    protected AbstractAddress(int size, String addr) {
        this(size,
            Arrays.stream(addr.split("\\."))
            .map( s ->  (byte) Integer.parseInt(s))
            .toArray(Byte[]::new));
    }

    public byte[] getBytes() {
        return bytes.clone(); // safely ensure calling code cannot modify
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
        int[] result = new int[size];
        for(int i = 0; i < size; i++) {
            result[i] = getUnsignedByte(i);
        }
        return result;
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
        AbstractAddress other = (AbstractAddress) obj;
        if (!Arrays.equals(bytes, other.bytes))
            return false;
        return true;
    }

    
}
