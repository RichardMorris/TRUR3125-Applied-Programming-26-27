package org.singsurf.jns3.network_layer;

/**
 * See https://www.rfc-editor.org/rfc/rfc1071.txt for the RFC
 * Implementation of 1's complement arithmetic in Java.
 * 
 * See https://stackoverflow.com/questions/66449588/what-is-1s-complement-addition
 * 
 * 
 * OnesComplement
 */
public class OnesComplementNumber {
    public static final Object MINUS_ZERO = new OnesComplementNumber((short) 0xFFFF);
	short num;

    public OnesComplementNumber(short num) {
        this.num = num;
    }

    public OnesComplementNumber(int num) {
        this.num = (short) (num & 0xFFFF);
    }

    public OnesComplementNumber(byte low, byte high) {
        this.num = (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }

    public short getValue() {
        return num;
    }

    public short twosComplementValue() {
        if(num >=0)
            return (short) num;
        else {
            return (short) (-abs());
        }
    }

    public OnesComplementNumber getComplement() {
        return new OnesComplementNumber((short) ~num);
    }

    public short abs() {
        return (short) (num >= 0 ? num : ~num);
    }

    public OnesComplementNumber add(OnesComplementNumber b) {
        return add(b.getValue());
    }

    public OnesComplementNumber add(short b) {
        int sum = (num & 0xFFFF) + (b & 0xFFFF);
        return new OnesComplementNumber((short) ((sum & 0xFFFF) + (sum >> 16)));
    }

    public OnesComplementNumber add(int b) {
            return add((short) (b & 0xFFFF));
    }

    public OnesComplementNumber add(byte low, byte high) {
        return add(new OnesComplementNumber(low, high));
    }


    public String toString() {
        return String.format("0x%04X %d", num & 0xFFFF, twosComplementValue());
    }
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OnesComplementNumber other = (OnesComplementNumber) obj;
        return num == other.num;
    }
    
    public int hashCode() {
        return Short.hashCode(num);
    }   

}
