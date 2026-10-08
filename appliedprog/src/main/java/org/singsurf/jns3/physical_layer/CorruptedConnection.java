package org.singsurf.jns3.physical_layer;

import java.util.Random;

import org.singsurf.jns3.data_link_layer.EthernetEndPoint;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.MACAddress;

/**
 * Every 10th frame is corrupted. 
 * One bit of either the
 * destination or source MAC address,
 * the EtherType, the payload, or frame check
 * is flipped. 
 */
public class CorruptedConnection implements EthernetConnection {
    EthernetEndPoint dev1;
    EthernetEndPoint dev2;
    private int count = 0;

    public CorruptedConnection(EthernetEndPoint dev1, EthernetEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
        dev2.setConnection(this.getReverseConnection());
    }

    private CorruptedConnection(EthernetEndPoint dev1, EthernetEndPoint dev2, boolean reversed) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this);
    }

    public void send(EthernetFrame frame) {
        count++;
        if (count % 10 != 0) {
            dev2.receiveFrame(frame);
        } else {
            dev2.receiveFrame(corrupt(frame));
        }
    }

    public EthernetConnection getReverseConnection() {
        return new CorruptedConnection(dev2, dev1, true);
    }

    byte modifyByte(byte b) {
        int bit = (int) (Math.random()*8);
        byte mask = (byte) (1 << bit);
        return (byte) (b ^ mask);
    }


    EthernetFrame corrupt(EthernetFrame frame) {
        var payload = frame.getPayload();
        var len = 6 + 6 + 2 + payload.length + 8;
        int pos = (int) (Math.random() * len);
        return corrupt(frame, pos);
    }

    EthernetFrame corrupt(EthernetFrame frame,int pos) {
        var dest = frame.getDestination();
        var src = frame.getSource();
        short type = frame.getEtherType();
        long check = frame.getFrameCheck();
        var payload = frame.getPayload();
        var newPayload = payload.clone();

        if(pos < 6) {
            byte[] destBytes = dest.getBytes();
            destBytes[pos] = modifyByte(destBytes[pos]);
            dest = new MACAddress(destBytes);
        } else if(pos < 12) {
            byte[] srcBytes = src.getBytes();
            srcBytes[pos - 6] = modifyByte(srcBytes[pos - 6]);
            src = new MACAddress(srcBytes);
        } else if(pos < 14) {
            type = (short) (type ^ (1 << ((pos - 12) * 8)));
        } else if(pos < 14 + payload.length) {
            newPayload[pos - 14] = modifyByte(newPayload[pos - 14]);
        } else {
            check = check ^ (1L << ((pos - 14 - payload.length) * 8));
        }
        return EthernetFrame.createRawFrame(dest, src, type, newPayload, check);
    }
}

