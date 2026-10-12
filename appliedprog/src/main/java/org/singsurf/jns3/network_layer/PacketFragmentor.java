package org.singsurf.jns3.network_layer;
import java.util.ArrayList;
import java.util.List;  
public class PacketFragmentor {
    final int maxLength; // the maximum length of of a packet
    final int dataSize; // the size of the data portion of each fragment
    final int offsetIncrement; // how much the offset should increase for each fragment
    
    int identificationCounter = 0; // to generate unique identification for each packet
    
    public PacketFragmentor(int maxLength) {
        this.maxLength = maxLength;
        var maxData = (maxLength - 20); // assuming 20 bytes for the header
        this.offsetIncrement = maxData / 8;
        this.dataSize = this.offsetIncrement * 8; // round down to the nearest multiple of 8
    }

    public List<IPPacket> getFragments(IPPacket packet) {
        List<IPPacket> fragments = new ArrayList<>();
        int packetLength = packet.getTotalLength();
        if(packetLength <= maxLength) {
            fragments.add(packet);
            return fragments;
        }
        ++identificationCounter;
        int offset = 0;
        int packetDataLength = packet.getData().length;
        while(packetDataLength - 8 * offset > dataSize) { 
            IPPacket fragment = packet.createFragment(offset, dataSize, true,identificationCounter);
            fragments.add(fragment);
            offset += offsetIncrement;
        }
        if(packetDataLength - 8 * offset > 0) { 
            IPPacket fragment = packet.createFragment(offset, packetDataLength - 8 * offset, false,identificationCounter);
            fragments.add(fragment);
            offset += offsetIncrement;
        }
        return fragments;
    }
}