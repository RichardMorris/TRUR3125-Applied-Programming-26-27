package org.singsurf.jns3.physical_layer;

/**
 * Every 10th frame is corrupted. 
 * One bit is flipped. 
 */
public class CorruptedConnection extends PhysicalConnection {
    private int count = 0;

    public CorruptedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        super(dev1, dev2);
    }

    public void send(byte[] frame, boolean reversed) {
        count++;
        if (count % 10 != 0) {
            receive(frame, reversed);
        } else {
            receive(corrupt(frame), reversed);
        }
    }

    byte modifyByte(byte b) {
        int bit = (int) (Math.random()*8);
        byte mask = (byte) (1 << bit);
        return (byte) (b ^ mask);
    }

    byte[] corrupt(byte[] frame) {
        var len = frame.length;
        int pos = (int) (Math.random() * len);
        return corrupt(frame, pos);
    }

    byte[] corrupt(byte[] frame,int pos) {
        byte[] newFrame = frame.clone();

        newFrame[pos] = modifyByte(newFrame[pos]);
        return newFrame;
    }
}

