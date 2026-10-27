package org.singsurf.jns3.network_layer;

import java.util.Arrays;

import org.singsurf.jns3.AbstractAddress;

/**
 * A 4 byte IP address like 138.253.39.2 
 */
public class IPAddress extends AbstractAddress {
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

    public int getIntValue() {
        var us = getUnsignedBytes();
        return (us[0] << 24) | (us[1] << 16) | (us[2] << 8) | us[3];
    }

    public short getHighWord() {
        return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
    }
    
    public short getLowWord() {
        return (short) ((bytes[2] << 8) | (bytes[3] & 0xFF));
    }
    
    public static IPAddress fromInt(int value) {
        return new IPAddress(new byte[] {
            (byte) ((value >> 24) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) (value & 0xFF)
        });
    }

    public String toString() {
        var us = getUnsignedBytes();
        return String.join(".",
            Arrays.stream(us)
            .mapToObj(i-> Integer.toString(i))
            .toList());
    }

}
