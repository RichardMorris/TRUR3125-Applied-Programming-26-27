package org.singsurf.jns3.data_link_layer;

import java.util.zip.CRC32;

public class EthernetFrame {
    MACAddress destination;
    MACAddress source;
    short etherType;
    byte[] payload;
    long frameCheck;

    public EthernetFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload) {
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.payload = payload;
        this.frameCheck = calcFrameCheck();
        if(etherType == ARPController.ARP_ETHER_TYPE)  
            return; // Skip further checks for ARP frames

        if(etherType <= 0 || etherType > 1500) {
            throw new IllegalArgumentException("Invalid etherType: " + etherType);
        }
        if(payload == null || payload.length != etherType) {
            throw new IllegalArgumentException("Invalid payload length: " + (payload == null ? 0 : payload.length));
        }
    }

    private EthernetFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload, long frameCheck) {
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.payload = payload;
        this.frameCheck = frameCheck;
    }

    public MACAddress getDestination() {
        return destination;
    }

    public MACAddress getSource() {
        return source;
    }

    public short getEtherType() {
        return etherType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadAsString() {
        return new String(payload);
    }
    
    public long calcFrameCheck() {
        CRC32 crc = new CRC32();
        crc.update(destination.getBytes());
        crc.update(source.getBytes());
        crc.update((byte)(etherType >> 8));
        crc.update((byte)(etherType & 0xFF));
        crc.update(payload);
        return crc.getValue();
    }
    public long getFrameCheck() {
        return frameCheck;
    }

    @Override
    public String toString() {
        return "EthernetFrame{" +
                "destination=" + destination +
                ", source=" + source +
                ", etherType=" + etherType +
                ", payload=" + new String(payload) +
                '}';
    }

    public static EthernetFrame createRawFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload, long frameCheck) {
            return new EthernetFrame(destination,source,etherType,payload,frameCheck);
    }


}
