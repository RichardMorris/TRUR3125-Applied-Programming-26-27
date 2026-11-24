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
}
