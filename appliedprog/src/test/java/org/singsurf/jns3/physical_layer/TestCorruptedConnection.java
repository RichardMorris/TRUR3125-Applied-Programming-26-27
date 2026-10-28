package org.singsurf.jns3.physical_layer;
import org.singsurf.jns3.hardware.NamedDevice;
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
import org.singsurf.jns3.data_link_layer.FrameReceiver;

public class TestCorruptedConnection {
    PC pc1;
    CorruptedConnection cc;
    EthernetEndPoint ep2;
    Receiver receiver;
    static class Receiver implements NamedDevice, MACAddressable, FrameReceiver {
        EthernetFrame lastReceivedFrame;
        MACAddress macAddress;
        String name;

        public MACAddress getMACAddress() {
            return macAddress;
        }
        Receiver() {
            this.name = "Receiver";
            macAddress = new DeviceFactory().createMACAddress();
        }
        public void receiveFrame(EthernetFrame frame) {
            lastReceivedFrame = frame;
        }

        public String getName() {
            return name;
        }

    }

    @BeforeEach
    public void setup() {
        Jns3Logger.clearLog();
        DeviceFactory deviceFactory = new DeviceFactory();
        pc1 = deviceFactory.createPC("PC1");
        receiver = new Receiver();
        ep2 = new EthernetEndPoint("EP2", receiver);
        cc = new CorruptedConnection(pc1.getEndPoint(), ep2);
        assertEquals(Jns3Logger.getLogItems().size(), 0);
    }


    @Test
    public void test_bit_twiddling() {
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
            receiver.getMACAddress(), 
            "Hello PC2 from PC1");
        int payload_len = frame.getContents().length;
        byte[] bytes = frame.getBytes();

        for(int i=0;i<6;++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes, i));
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getDestination(), frame0.getDestination());
        }
        for(int i=6;i<12;++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes, i));
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getSource(), frame0.getSource());
        }
        for(int i=12;i<14;++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes,i));
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getEtherType(), frame0.getEtherType());
        }
        for(int i=14; i<14+payload_len; ++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes,i));
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getContents()[i-14], frame0.getContents()[i-14]);
            assertNotEquals(frame.getPayloadAsString(), frame0.getPayloadAsString());
        }
        for(int i=14+payload_len; i<14+payload_len+4; ++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes,i));
            assertNotEquals(frame, frame0);
            assertNotEquals(frame.getFrameCheck(), frame0.getFrameCheck());
        }
    }

    @Test
    public void test_corrupt_CRC() {
        var frame = pc1.createEthernetFrame(
            receiver.getMACAddress(), 
            "Hello PC2 from PC1");
        byte[] bytes = frame.getBytes();

        for(int i=0; i<14+frame.getContents().length+4; ++i) {
            var frame0 = EthernetFrame.parseBytes(cc.corrupt(bytes, i));
            assertNotEquals(frame0.getFrameCheck(), frame0.calcFrameCheck());
        }
    }

    @Test
    public void every_10th_frame_is_corrupted() {
        for(int i=1; i<=100;++i) {
            var frame = pc1.createEthernetFrame(
                receiver.getMACAddress(), 
                "Hello PC2 from PC1 no " + i);
            pc1.sendFrame(frame);
            var frame0 = receiver.lastReceivedFrame;
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
