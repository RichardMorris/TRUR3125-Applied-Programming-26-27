package org.singsurf.jns3.data_link_layer;

public class EthernetFrame {
    MACAddress destination;
    MACAddress source;
    short etherType;
    byte[] payload;

    public EthernetFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload) {
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.payload = payload;

        if(etherType == ARPController.ARP_ETHER_TYPE)  
            return; // Skip further checks for ARP frames

        if(etherType <= 0 || etherType > 1500) {
            throw new IllegalArgumentException("Invalid etherType: " + etherType);
        }
        if(payload == null || payload.length != etherType) {
            throw new IllegalArgumentException("Invalid payload length: " + (payload == null ? 0 : payload.length));
        }
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

    @Override
    public String toString() {
        return "EthernetFrame{" +
                "destination=" + destination +
                ", source=" + source +
                ", etherType=" + etherType +
                ", payload=" + new String(payload) +
                '}';
    }
}
