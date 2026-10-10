package org.singsurf.jns3.network_layer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIPAddress {
    @Test 
    public void testGetIntValue() {
        IPAddress ip = new IPAddress(new byte[] {127, 0, 0, 1});
        assertEquals(0x7F000001, ip.getIntValue());

        IPAddress ip2 = IPAddress.fromInt(0x7F000001);
        assertEquals(0x7F000001, ip2.getIntValue()); 
        assertEquals(ip, ip2);
        short high = ip.getHighWord();
        short low = ip.getLowWord();
        assertEquals(0x7F00, high);
        assertEquals(0x0001, low);

        ip = new IPAddress(new int[] {138, 253, 39, 2});
        assertEquals(0x8AFD2702, ip.getIntValue());
        IPAddress ip3 = IPAddress.fromInt(0x8AFD2702);
        assertEquals(0x8AFD2702, ip3.getIntValue());
        assertEquals(ip, ip3);
        short high3 = ip3.getHighWord();
        short low3 = ip3.getLowWord();
        assertEquals((short) 0x8AFD, high3);
        assertEquals((short) 0x2702, low3);
    }
}
