package org.singsurf.jns3.data_link_layer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestARPMessage {
    @Test
    public void testCreateGarpRequest() {
        MACAddress senderMac = new MACAddress(new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55});
        IPAddress  senderIp = new IPAddress("192.168.1.1");
        ARPMessage garRequest = ARPMessage.createGarpRequest(senderMac, senderIp);
        assertEquals(senderMac, garRequest.getSenderMac());
        assertEquals(senderIp, garRequest.getSenderIp());
        assertEquals(new MACAddress(new byte[6]), garRequest.getTargetMac());
        assertEquals(senderIp, garRequest.getTargetIp());

        byte[] expectedBytes = new byte[28];
        expectedBytes[0] = 0x00; // Hardware type: Ethernet
        expectedBytes[1] = 0x01;
        expectedBytes[2] = 0x08; // Protocol type: IPv4
        expectedBytes[3] = 0x00;
        expectedBytes[4] = 0x06; // Hardware size: 6
        expectedBytes[5] = 0x04; // Protocol size: 4
        expectedBytes[6] = 0x00; // Opcode: request (1)
        expectedBytes[7] = 0x01; // Set based on isRequest (1 for request, 2 for reply)
        expectedBytes[8] = 0x00; // Sender MAC address
        expectedBytes[9] = 0x11;
        expectedBytes[10] = 0x22;
        expectedBytes[11] = 0x33;
        expectedBytes[12] = 0x44;
        expectedBytes[13] = 0x55;
        expectedBytes[14] = (byte) 192; // Sender IP address
        expectedBytes[15] = (byte) 168;
        expectedBytes[16] = 1;
        expectedBytes[17] = 1;
        expectedBytes[18] = 0x00; // Target MAC address
        expectedBytes[19] = 0x00;
        expectedBytes[20] = 0x00;
        expectedBytes[21] = 0x00;
        expectedBytes[22] = 0x00;
        expectedBytes[23] = 0x00;
        expectedBytes[24] = (byte) 192; // Target IP address
        expectedBytes[25] = (byte) 168;
        expectedBytes[26] = 1;
        expectedBytes[27] = 1;
        
        
        assertArrayEquals(expectedBytes, garRequest.toBytes());
    }
}
