package org.singsurf.jns3.network_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestOnesComplement {
    @Test
    public void test_add() {
        // example from https://stackoverflow.com/questions/66449588/what-is-1s-complement-addition
        var a = new OnesComplementNumber(0xF000);
        var b = new OnesComplementNumber(0x1009);
        var result = a.add(b);
        assertEquals(new OnesComplementNumber(0x000A), result);
        result = b.add(a);
        assertEquals(new OnesComplementNumber(0x000A), result);
        
        var a2 = new OnesComplementNumber(0x8000);
        var b2 = new OnesComplementNumber(0x8000);
        result = a2.add(b2);
        assertEquals(new OnesComplementNumber(0x0001), result);

        // adding minus zero should yield the same number
        a2 = new OnesComplementNumber(0x1234);
        var minusZero = new OnesComplementNumber(0xFFFF);
        result = a2.add(minusZero);
        assertEquals(new OnesComplementNumber(0x1234), result);

        var a3 = new OnesComplementNumber(0xA234);
        result = a3.add(minusZero);
        assertEquals(new OnesComplementNumber(0xA234), result);

        // adding a number to it complement should yield minus zero
        var a4 = a2.getComplement();
        result = a2.add(a4);
        assertEquals(minusZero, result);

        a4 = a3.getComplement();
        result = a3.add(a4);
        assertEquals(minusZero, result);

        // minus zero + minus zero should yield minus zero
        result = minusZero.add(minusZero);
        assertEquals(minusZero, result);
    }

    @Test
    public void test_twosComplement() {
        var a = new OnesComplementNumber(0x1234);
        var result = a.twosComplementValue();
        assertEquals((short) 0x1234, result);

        var b = new OnesComplementNumber(0xF000);
        result = b.twosComplementValue();
        assertEquals((short) -4095, result);

        var c = b.getComplement();
        result = c.twosComplementValue();
        assertEquals((short) 4095, result);

        var minusZero = new OnesComplementNumber(0xFFFF);
        result = minusZero.twosComplementValue();
        assertEquals((short) 0, result);

    }
    
    @Test
    public void test_comparator() {
    	OnesComplementNumber[] numbers = {
				OnesComplementNumber.fromTwosComplement(-127),
				OnesComplementNumber.fromTwosComplement(-126),
				OnesComplementNumber.fromTwosComplement(-2),
				OnesComplementNumber.fromTwosComplement(-1),
				OnesComplementNumber.MINUS_ZERO,
				new OnesComplementNumber(0),
				new OnesComplementNumber(1),
				new OnesComplementNumber(2),
				new OnesComplementNumber(126),
				new OnesComplementNumber(127)
		};
    	for(int i=0; i<numbers.length; i++) {
			for(int j=0; j<numbers.length; j++) {
				int expected = Integer.compare(i, j);
				int actual = numbers[i].compareTo(numbers[j]);
				assertEquals(expected, actual, "Comparing " + numbers[i] + " and " + numbers[j]);
			}
		}


	}

    @Test
    public void ones_and_twos_table() {
        var itt = OnesComplementNumber.iterator(-16, (short) -1, 1);
        System.out.println("Num\t1's C\t2's C");
		while(itt.hasNext()) {
			var num = itt.next();
            var parts = num.toString().split(" ");
			System.out.format("%3s\t%s\t0x%04X%n", parts[1], parts[0], (short) num.twosComplementValue());
		}
        {
            var num = OnesComplementNumber.MINUS_ZERO;
            var parts = num.toString().split(" ");
		    System.out.format("%3s\t%s%n", parts[1], parts[0]);
        }
        itt = OnesComplementNumber.iterator(0, (short) 16, 1);
        while(itt.hasNext()) {
            var num = itt.next();
                var parts = num.toString().split(" ");
                System.out.format("%3s\t%s\t0x%04X%n", parts[1], parts[0], (short) num.twosComplementValue());
        }

    }
    
    @Test
    public void test_completeIterator() {
    	var itt = OnesComplementNumber.completeIterator(-3, (short) 3, 1);
    	StringBuilder sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	var lines = sb.toString().split("\n");
    	assertEquals(8, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFD -2", lines[1].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0xFFFF -0", lines[3].trim());
    	assertEquals("0x0000 0", lines[4].trim());
    	assertEquals("0x0001 1", lines[5].trim());
    	assertEquals("0x0002 2", lines[6].trim());
    	assertEquals("0x0003 3", lines[7].trim());

    	itt = OnesComplementNumber.completeIterator(-3, 3, 2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFE -1", lines[1].trim());
    	assertEquals("0x0000 0", lines[2].trim());
    	assertEquals("0x0002 2", lines[3].trim());

    	itt = OnesComplementNumber.completeIterator(-32767, -32765, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x8000 -32767", lines[0].trim());
    	assertEquals("0x8001 -32766", lines[1].trim());
    	assertEquals("0x8002 -32765", lines[2].trim());

    	itt = OnesComplementNumber.completeIterator(32765, 32767, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x7FFD 32765", lines[0].trim());
    	assertEquals("0x7FFE 32766", lines[1].trim());
    	assertEquals("0x7FFF 32767", lines[2].trim());

    	itt = OnesComplementNumber.completeIterator(-3, 0, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(5, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFD -2", lines[1].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0xFFFF -0", lines[3].trim());
    	assertEquals("0x0000 0", lines[4].trim());

    	itt = OnesComplementNumber.completeIterator(0, 3, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0x0000 0", lines[0].trim());
    	assertEquals("0x0001 1", lines[1].trim());
    	assertEquals("0x0002 2", lines[2].trim());
    	assertEquals("0x0003 3", lines[3].trim());

    	itt = OnesComplementNumber.completeIterator(3, -3, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(8, lines.length);
    	assertEquals("0xFFFC -3", lines[7].trim());
    	assertEquals("0xFFFD -2", lines[6].trim());
    	assertEquals("0xFFFE -1", lines[5].trim());
    	assertEquals("0xFFFF -0", lines[4].trim());
    	assertEquals("0x0000 0", lines[3].trim());
    	assertEquals("0x0001 1", lines[2].trim());
    	assertEquals("0x0002 2", lines[1].trim());
    	assertEquals("0x0003 3", lines[0].trim());
    	
    	itt = OnesComplementNumber.completeIterator(-32765, -32767, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x8000 -32767", lines[2].trim());
    	assertEquals("0x8001 -32766", lines[1].trim());
    	assertEquals("0x8002 -32765", lines[0].trim());

    	itt = OnesComplementNumber.completeIterator(32767, 32765, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x7FFD 32765", lines[2].trim());
    	assertEquals("0x7FFE 32766", lines[1].trim());
    	assertEquals("0x7FFF 32767", lines[0].trim());

    	itt = OnesComplementNumber.completeIterator(3, -3, -2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFD -2", lines[3].trim());
    	assertEquals("0xFFFF -0", lines[2].trim());
    	assertEquals("0x0001 1", lines[1].trim());
    	assertEquals("0x0003 3", lines[0].trim());

    	itt = OnesComplementNumber.completeIterator(2, -3, -2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFC -3", lines[3].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0x0000 0", lines[1].trim());
    	assertEquals("0x0002 2", lines[0].trim());

    }
    
    @Test
    public void test_iterator() {
    	var itt = OnesComplementNumber.iterator(-3, (short) 3, 1);
    	StringBuilder sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	var lines = sb.toString().split("\n");
    	assertEquals(7, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFD -2", lines[1].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0xFFFF -0", lines[3].trim());
    	assertEquals("0x0001 1", lines[4].trim());
    	assertEquals("0x0002 2", lines[5].trim());
    	assertEquals("0x0003 3", lines[6].trim());

    	itt = OnesComplementNumber.iterator(-3, 3, 2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFE -1", lines[1].trim());
    	assertEquals("0x0001 1", lines[2].trim());
    	assertEquals("0x0003 3", lines[3].trim());

    	itt = OnesComplementNumber.iterator(-32767, -32765, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x8000 -32767", lines[0].trim());
    	assertEquals("0x8001 -32766", lines[1].trim());
    	assertEquals("0x8002 -32765", lines[2].trim());

    	itt = OnesComplementNumber.iterator(32765, 32767, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x7FFD 32765", lines[0].trim());
    	assertEquals("0x7FFE 32766", lines[1].trim());
    	assertEquals("0x7FFF 32767", lines[2].trim());

    	itt = OnesComplementNumber.iterator(-3, 0, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFC -3", lines[0].trim());
    	assertEquals("0xFFFD -2", lines[1].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0xFFFF -0", lines[3].trim());

    	itt = OnesComplementNumber.iterator(0, 3, 1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFF -0", lines[0].trim());
    	assertEquals("0x0001 1", lines[1].trim());
    	assertEquals("0x0002 2", lines[2].trim());
    	assertEquals("0x0003 3", lines[3].trim());

    	itt = OnesComplementNumber.iterator(3, -3, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(7, lines.length);
    	assertEquals("0xFFFC -3", lines[6].trim());
    	assertEquals("0xFFFD -2", lines[5].trim());
    	assertEquals("0xFFFE -1", lines[4].trim());
    	assertEquals("0xFFFF -0", lines[3].trim());
    	assertEquals("0x0001 1", lines[2].trim());
    	assertEquals("0x0002 2", lines[1].trim());
    	assertEquals("0x0003 3", lines[0].trim());
    	
    	itt = OnesComplementNumber.iterator(-32765, -32767, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x8000 -32767", lines[2].trim());
    	assertEquals("0x8001 -32766", lines[1].trim());
    	assertEquals("0x8002 -32765", lines[0].trim());

    	itt = OnesComplementNumber.iterator(32767, 32765, -1);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
    		var num = itt.next();
    		sb.append(num.toString()).append("\n");
    	}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0x7FFD 32765", lines[2].trim());
    	assertEquals("0x7FFE 32766", lines[1].trim());
    	assertEquals("0x7FFF 32767", lines[0].trim());

    	itt = OnesComplementNumber.iterator(3, -3, -2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(4, lines.length);
    	assertEquals("0xFFFC -3", lines[3].trim());
    	assertEquals("0xFFFE -1", lines[2].trim());
    	assertEquals("0x0001 1", lines[1].trim());
    	assertEquals("0x0003 3", lines[0].trim());

    	itt = OnesComplementNumber.iterator(2, -3, -2);
    	sb = new StringBuilder();
    	while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString()).append("\n");
		}
    	lines = sb.toString().split("\n");
    	assertEquals(3, lines.length);
    	assertEquals("0xFFFD -2", lines[2].trim());
    	assertEquals("0xFFFF -0", lines[1].trim());
    	assertEquals("0x0002 2", lines[0].trim());

    }

}
