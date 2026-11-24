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

	/**
	 * Creates a new OnesComplementNumber with the specified value.
	 * So {@code fromTwosComplement(-1)} returns {@code 0xFFFF}
	 * and {@code fromTwosComplement(0)} returns {@code 0x0000}.
	 * @see #fromTwosComplement(int)
	 * @param num the value of the number in 1's complement representation
	 */
    public OnesComplementNumber(short num) {
        this.num = num;
    }

	/**
	 * Creates a new OnesComplementNumber with the specified value.
	 * Uses the lower 16 bits of the integer to represent the number in 1's complement.
	 * @param num the value of the number in 1's complement representation
	 */
    public OnesComplementNumber(int num) {
        this.num = (short) (num & 0xFFFF);
    }

    /**
	 * Creates a new OnesComplementNumber from two bytes representing the low and high parts of the number.
	 * @param low the low byte of the number
	 * @param high the high byte of the number
	 */
    public OnesComplementNumber(byte low, byte high) {
        this.num = (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }

    /**
	 * Returns the value of the number in 1's complement representation.
	 */
    public short getValue() {
        return num;
    }

    /**
     * Returns the value of the number in 2's complement representation.
     * @return the 2's complement value of the number
     */
    public short twosComplementValue() {
        if(num >=0)
            return (short) num;
        else {
            return (short) (-abs());
        }
    }

    /**
	 * Returns the negation of the number, the bit wise NOT of the current number.
	 * @return a new OnesComplementNumber representing the 1's complement of the current number
	 */
    public OnesComplementNumber getComplement() {
        return new OnesComplementNumber((short) ~num);
    }

    /**
	 * Returns the absolute value of the number.
	 */
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

    /**
	 * Adds the contents of a byte array to the current OnesComplementNumber.
	 * Treating byte as big-endian words, padding with zero if the length is odd.
	 * @param contents the byte array to be added
	 * @return a new OnesComplementNumber representing the sum of the current number and the contents of the byte array
	 */
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
    
    /**
	 * Compares this OnesComplementNumber to another object for equality.
	 * -0 and 0 are not considered equal.
	 * @param obj the object to compare with
	 * @return true if the other object is a OnesComplementNumber with the same value, false otherwise
	 **/
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OnesComplementNumber other = (OnesComplementNumber) obj;
        return num == other.num;
    }
    
    public int hashCode() {
        return Short.hashCode(num);
    }
    
    /**
     * Compares this OnesComplementNumber with another for order.
     * -0 is considered less than +0.
     */
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

    /**
	 * Creates a OnesComplementNumber from a 2's complement integer value.
	 * So {@code fromTwosComplement(-1)} returns {@code 0xFFFE}
	 * and {@code fromTwosComplement(0)} returns {@code 0x0000}.
	 * @param value the 2's complement integer value to convert
	 * @return a new OnesComplementNumber representing the 1's complement of the given 2's complement value
	 */
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
