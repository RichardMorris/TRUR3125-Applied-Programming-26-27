package org.singsurf.jns3.physical_layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.data_link_layer.EthernetEndPoint;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.data_link_layer.MACAddressable;

public class TestCorruptedConnection {
    PC pc1;
    Receiver pc2;
    CorruptedConnection cc;
    
    static class Receiver extends EthernetEndPoint implements MACAddressable {
        EthernetFrame lastReceivedFrame;
        MACAddress macAddress;

        public MACAddress getMACAddress() {
            return macAddress;
        }
        Receiver() {
            super("Receiver");
            macAddress = new DeviceFactory().createMACAddress();
        }
        public void receiveFrame(EthernetFrame frame) {
            lastReceivedFrame = frame;
        }
    }

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        pc2 = new Receiver();
        cc = new CorruptedConnection(pc1, pc2);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }


    @Test
    public void test_bit_twidling() {
        byte zeros = (byte) 0;
        byte ones = (byte) 0xFF;
        for(int bit=0;bit<8;++bit) {
            byte mask = (byte) (1 << bit);
            byte toggledZeros = (byte) (zeros ^ mask);
            byte toggledOnes = (byte) (ones ^ mask);
            System.out.printf("Bit %d: toggledZeros = 0x%02X, toggledOnes = 0x%02X%n", bit, toggledZeros, toggledOnes); 
            assertNotEquals(zeros, toggledZeros);
            assertNotEquals(ones, toggledOnes);
        }
    }
    
    @Test
    public void test_modify_byte() {
        for(int i=0;i<256;++i) {
            byte b = (byte) i;
            byte mod = cc.modifyByte(b);
            assertNotEquals(b, mod);
        }
    }

    @Test
    public void test_modify_frame() {
        var frame = pc1.createEthernetFrame(
            pc2.getMACAddress(), 
            "Hello PC2 from PC1");
        int payload_len = frame.getPayload().length;

        for(int i=0;i<6;++i) {
            var frame0 = cc.corrupt(frame, i);
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getDestination(), frame0.getDestination());
        }
        for(int i=6;i<12;++i) {
            var frame0 = cc.corrupt(frame, i);
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getSource(), frame0.getSource());
        }
        for(int i=12;i<14;++i) {
            var frame0 = cc.corrupt(frame,i);
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getEtherType(), frame0.getEtherType());
        }
        for(int i=14; i<14+payload_len; ++i) {
            var frame0 = cc.corrupt(frame,i);
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getPayload()[i-14], frame0.getPayload()[i-14]);
            assertNotEquals(frame.getPayloadAsString(), frame0.getPayloadAsString());
        }
        for(int i=14+payload_len; i<14+payload_len+8; ++i) {
            var frame0 = cc.corrupt(frame,i);
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getFrameCheck(), frame0.getFrameCheck());
        }
    }

    @Test
    public void test_corrupt_CRC() {
        var frame = pc1.createEthernetFrame(
            pc2.getMACAddress(), 
            "Hello PC2 from PC1");

        for(int i=0; i<14+frame.getPayload().length+8; ++i) {
            var frame0 = cc.corrupt(frame, i);
            assertNotEquals(frame0.getFrameCheck(), frame0.calcFrameCheck());
        }
    }

    @Test
    public void every_10th_frame_is_corrupted() {
        for(int i=1; i<=100;++i) {
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello PC2 from PC1 no " + i);
            pc1.sendFrame(frame);
            var frame0 = pc2.lastReceivedFrame;
            if(i % 10 == 0) {
                assertNotEquals(frame, frame0);
                assertNotEquals(frame0.getFrameCheck(), frame0.calcFrameCheck());
            } else {
                assertEquals(frame, frame0);
                assertEquals(frame0.getFrameCheck(), frame0.calcFrameCheck());
            }
        }
    }
}
