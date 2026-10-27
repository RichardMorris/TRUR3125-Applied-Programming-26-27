package org.singsurf.jns3.network_layer;
import java.util.Iterator;
/**
 * Class representing a number in 1's complement representation.
 * <p>
 * See https://www.rfc-editor.org/rfc/rfc1071.txt for the RFC. 
 * See https://stackoverflow.com/questions/66449588/what-is-1s-complement-addition
 */
public class OnesComplementNumber implements Comparable<OnesComplementNumber> {
    public static final OnesComplementNumber MINUS_ZERO = new OnesComplementNumber((short) 0xFFFF);
    public static final OnesComplementNumber MIN_VALUE = new OnesComplementNumber((short) 0x8000);
    public static final OnesComplementNumber MAX_VALUE = new OnesComplementNumber((short) 0x7FFF);
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
    
    public boolean isPositive() {
		return num >= 0;
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
    
    static OnesComplementNumber fromTwosComplement(int value) {
    	if(value>=0) {	
    		return new OnesComplementNumber((short) value);
    	} else {
    		return new OnesComplementNumber((short) ~(-value));
		}
	}

    public static Iterator<OnesComplementNumber> iterator(int start, int stop, int stepsize) {
        
        return new Iterator<OnesComplementNumber>() {
        	OnesComplementNumber stepSize = fromTwosComplement(stepsize);
        	OnesComplementNumber currentValue = fromTwosComplement(start);
        	OnesComplementNumber stopValue = fromTwosComplement(stop);
        	boolean hasNext = true;
            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public OnesComplementNumber next() {
            	var res = currentValue;
        		var nextValue = currentValue.add(stepSize);
        		if(nextValue.compareTo(stopValue) > 0) {
					hasNext = false;
				}        		
        		currentValue = nextValue;
        		return res;
            }
        };
    }

	@Override
	public int compareTo(OnesComplementNumber that) {
		if(num>=0) {
			if(that.num>=0) {
				return Short.compare(num, that.num);
			} else {
				return 1;
			}
		} else {
			if(that.num>=0) {
				return -1;
			}
			return Short.compare(that.num,num);
		}
	}
}
