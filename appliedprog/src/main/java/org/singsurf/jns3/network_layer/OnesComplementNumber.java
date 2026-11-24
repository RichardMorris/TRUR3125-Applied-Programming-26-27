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

	public OnesComplementNumber add(byte[] contents) {
		
		if(contents.length == 0) {
			return this;
		} else if(contents.length == 1) {
			return add((short) (contents[0] & 0xFF));
		} else {
			short sum = 0;
			for(int i=0; i<contents.length; i+=2) {
				short word = (short) ((contents[i] & 0xFF) << 8);
				if(i+1 < contents.length) {
					word |= (contents[i+1] & 0xFF);
				}
				sum += word;
			}
			return add(sum);
		}
	}
	


    public String toString() {
        if(this.equals(MINUS_ZERO)) {
            return String.format("0x%04X -%d", num & 0xFFFF, twosComplementValue());
        }
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
    
	@Override
	public int compareTo(OnesComplementNumber that) {
		if(num>=0) {
			if(that.num>=0) {
				var val = Short.compare(num, that.num);
				return val >0 ? 1 : val <0 ? -1 : 0;
			} else {
				return 1;
			}
		} else if(num == OnesComplementNumber.MINUS_ZERO.num) {
			if(that.num >= 0) {
				return -1;
			} else if(that.num == OnesComplementNumber.MINUS_ZERO.num) {
				return 0;
			}
			return 1;
		} else {
			if(that.num>=0) {
				return -1;
			}
			var val =  Short.compare(that.abs(),abs());
			return val >0 ? 1 : val <0 ? -1 : 0;
		}
	}
	
	/**
	 * Creates an iterator over a range of 1's complement numbers with a single zero value (0xFFFF -1).
	 * @param start 2's complement first value in the range (inclusive) must be in the range -32767 to 32767
	 * @param stop 2's complement last value in the range (inclusive) must be in the range -32767 to 32767
	 * @param stepsize 2's complement step size for the iteration can be positive or negative but not zero.
	 * 
	 * @return an iterator over the range of 1's complement numbers.
	 * @throws IllegalArgumentException if the step size is zero or if the start or stop values are out of range for 16-bit signed integers.
	 */
    public static Iterator<OnesComplementNumber> iterator(int start, int stop, int stepsize) {
    	if(stepsize == 0) {
    		throw new IllegalArgumentException("Step size cannot be zero");
    	}
         return new Iterator<OnesComplementNumber>() {
         	OnesComplementNumber stepSize = fromTwosComplement(stepsize);
        	OnesComplementNumber currentValue = start == 0 ? OnesComplementNumber.MINUS_ZERO : fromTwosComplement(start);
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
        		if(stepsize > 0) {
        			if(nextValue.compareTo(stopValue) > 0 || (currentValue.isPositive() && ! nextValue.isPositive())) {
        				hasNext = false;
        			}
				}  else {
					if(nextValue.compareTo(stopValue) < 0 || (!currentValue.isPositive() && nextValue.isPositive())) {
						hasNext = false;
					}
				}
        		currentValue = nextValue;
        		return res;
            }
        };
    }

	/**
	 * Creates an iterator over a range of 1's complement numbers with a two zero values (0xFFFF -0, 0x0000 +0).
	 * @param start 2's complement first value in the range (inclusive) must be in the range -32767 to 32767
	 * @param stop 2's complement last value in the range (inclusive) must be in the range -32767 to 32767
	 * @param stepsize 2's complement step size for the iteration can be positive or negative but not zero.
	 * 
	 * @return an iterator over the range of 1's complement numbers.
	 * @throws IllegalArgumentException if the step size is zero or if the start or stop values are out of range for 16-bit signed integers.
	 */

    public static Iterator<OnesComplementNumber> completeIterator(int start, int stop, int stepsize) {
        return new Iterator<OnesComplementNumber>() {
        	OnesComplementNumber stepSize = new OnesComplementNumber(stepsize);
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
            	OnesComplementNumber nextValue;
        		if(stepsize > 0) {
                	nextValue = new OnesComplementNumber((short) (currentValue.num + stepSize.num));
					if(nextValue.compareTo(stopValue) > 0 || (currentValue.isPositive() && ! nextValue.isPositive())) {
						hasNext = false;
					}
				} else {
	            	nextValue = new OnesComplementNumber((short) (currentValue.num + stepSize.num));
	            	if(nextValue.compareTo(stopValue) < 0 || (!currentValue.isPositive() && nextValue.isPositive())) {
						hasNext = false;
					}
				}
        		currentValue = nextValue;
        		return res;
            }
        };
    }

    static OnesComplementNumber fromTwosComplement(int value) {
    	if(value > 32767 || value < -32767) {
			throw new IllegalArgumentException("Value out of range for 16-bit signed integer: " + value);
		}
    	if(value>=0) {	
    		return new OnesComplementNumber((short) value);
    	} else {
    		return new OnesComplementNumber((short) ~(-value));
		}
	}


}
