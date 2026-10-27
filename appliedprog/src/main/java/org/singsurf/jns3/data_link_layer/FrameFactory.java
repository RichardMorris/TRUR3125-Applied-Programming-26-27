package org.singsurf.jns3.data_link_layer;

/**
 * A factory class for creating Ethernet frames.
 */
public class FrameFactory {
	static FrameFactory instance = new FrameFactory();
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
     * A Factory method to create an Ethernet frame. the length of the payload is used as the EtherType field.
     * @param destination the destination MAC address
     * @param source the source MAC address
     * @param payload the payload of the Ethernet frame
     * @return the created Ethernet frame
     */
    public EthernetFrame createEthernetFrame(
        MACAddress destination,
        MACAddress source,
        byte[] payload) {
        return new EthernetFrame(destination, source, (short) payload.length, payload);
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

    public static FrameFactory getInstance() {
		return instance;
	}

}
