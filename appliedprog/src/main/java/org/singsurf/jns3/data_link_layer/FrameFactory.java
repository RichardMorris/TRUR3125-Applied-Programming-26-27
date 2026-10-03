package org.singsurf.jns3.data_link_layer;

import java.util.ArrayList;
import java.util.List;

public class FrameFactory {

    /**
     * A Factory method to create an Ethernet frame.
     * @param destination the destination MAC address
     * @param source the source MAC address
     * @param etherType the EtherType field
     * @param payload the payload of the Ethernet frame
     * @return the created Ethernet frame
     */
    public EthernetFrame createEthernetFrame(
        MACAddress destination,
        MACAddress source,
        short etherType,
        byte[] payload) {
        return new EthernetFrame(destination, source, etherType, payload);
    }

    /**
     * A Factory method to create an Ethernet frame from a message string.
     * @param destination the destination MAC address
     * @param source the source MAC address
     * @param message the message to be converted into the payload
     * @return the created Ethernet frame
     * @throws IllegalArgumentException if the message is too long to fit in a single Ethernet frame
     */
    public EthernetFrame createEthernetFrame(
        MACAddress destination,
        MACAddress source,
        String message) {

        byte[] payload = message.getBytes();
        short etherType = (short) payload.length;
        return new EthernetFrame(destination, source, etherType, payload);
    }

    /**
     * A Factory method to create multiple Ethernet frames from a message string.
     * @param destination the destination MAC address
     * @param source the source MAC address
     * @param message the message to be converted into the payload
     * @return the list of created Ethernet frames
     */
    public List<EthernetFrame> createEthernetFrames(
        MACAddress destination,
        MACAddress source,
        String message) {

        byte[] payload = message.getBytes();
        List<EthernetFrame> frames = new ArrayList<>();
        int pos = 0;
        while(pos < payload.length) {
            int chunkSize = Math.min(1500, payload.length - pos);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(payload, pos, chunk, 0, chunkSize);
            frames.add(new EthernetFrame(destination, source, (short) chunkSize, chunk));
            pos += chunkSize;
        }
        return frames;
    }

}
