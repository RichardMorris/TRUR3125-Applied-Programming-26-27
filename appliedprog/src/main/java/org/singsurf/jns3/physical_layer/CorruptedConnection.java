package org.singsurf.jns3.physical_layer;

/**
 * Every 10th frame is corrupted. 
 * One bit is flipped. 
 */
public class CorruptedConnection implements PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;
    private int count = 0;

    public CorruptedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private CorruptedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(byte[] frame) {
        count++;
        if (count % 10 != 0) {
            dev2.receive(frame);
        } else {
            dev2.receive(corrupt(frame));
        }
    }

    public PhysicalConnection getReverseConnection() {
        return new CorruptedConnection(dev2, dev1, true);
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

