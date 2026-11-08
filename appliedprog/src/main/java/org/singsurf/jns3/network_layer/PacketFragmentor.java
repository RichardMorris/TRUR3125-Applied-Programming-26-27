package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.List;  

/**
 * Fragment into smaller packets of a given maximum size.
 * Each set of fragments is given a unique identification number.
 */
public class PacketFragmentor {
	public static final int MAX_PACKET_SIZE = 576; // 576 bytes is the minimum MTU for IP packets


	int maxLength; // the maximum length of of a packet
    int dataSize; // the size of the data portion of each fragment
    int offsetIncrement; // how much the offset should increase for each fragment
    
    int identificationCounter = 0; // to generate unique identification for each packet
    
    public PacketFragmentor() {
        this.maxLength = MAX_PACKET_SIZE;
        var maxData = (maxLength - 20); // assuming 20 bytes for the header
        this.offsetIncrement = maxData / 8;
        this.dataSize = this.offsetIncrement * 8; // round down to the nearest multiple of 8
    }

	void setMaxLength(int size) {
        this.maxLength = size;
        var maxData = (maxLength - 20); // assuming 20 bytes for the header
        this.offsetIncrement = maxData / 8;
        this.dataSize = this.offsetIncrement * 8; // round down to the nearest multiple of 8
	}

    /**
     * Get the set of IP packet fragments. 
     * @param packet The original packet to fragments.
     * @return Either the original packet if it is small enough,
     * or a list of fragments if it is too large.
     */
    public List<IPPacket> getFragments(IPPacket packet) {
        List<IPPacket> fragments = new ArrayList<>();
        int packetLength = packet.getTotalLength();
        if(packetLength <= maxLength) {
            fragments.add(packet);
            return fragments;
        }
        ++identificationCounter;
        int offset = 0;
        int packetDataLength = packet.getContents().length;
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