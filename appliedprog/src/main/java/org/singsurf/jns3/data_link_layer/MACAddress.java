package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.AbstractAddress;

public class MACAddress extends AbstractAddress {
    public MACAddress(byte[] bytes) {
        super(6, bytes);
    }

    public MACAddress(int[] bytes) {
        super(6, bytes);
    }

    private static byte[] parseMacAddress(String addr) {
        String[] parts = addr.split("[:-]");
        if(parts.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address: " + addr);
        }
        byte[] bytes = new byte[6];
        for(int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return bytes;
    }

    public MACAddress(String addr) {
        super(6, parseMacAddress(addr));
    }

    @Override
    public String toString() {
        byte[] bytes = getBytes();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++) {
            if(i > 0) sb.append(":");
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

}
