package org.singsurf.jns3.data_link_layer;

import org.singsurf.jns3.network_layer.IPAddress;

/**
 * Represents an ARP message.
 * 
 * An `ARPMessage` message has the format:
 * <ul>
    * <li>2 byte hardware type. Ethernet is 0x0001.
    * <li>2 byte protocol type, IPv4 is 0x0800.
    * <li>1 byte length of hardware address, 6 for MAC addresses for Ethernet
    * <li>1 byte length of protocol address, 4 for IPv4 addresses.
    * <li>2 bytes for operation, 0x0001 for a request 0x0002 for a reply
    * <li>6 bytes for sender MacAddress
    * <li>4 bytes for sender IPv4 protocol address
    * <li>6 bytes for target MacAddress
    * <li>4 bytes for target IPv4 protocol address
    * </ul>
    * 28 bytes in total.
 * 
 * See <a href="../../../../../../Week5.md">Week 5</a> for details on the ARP message format.
 */
public class ARPMessage {
    private boolean isRequest;
    private MACAddress senderMac;
    private MACAddress targetMac;
    private IPAddress senderIp;
    private IPAddress targetIp;
    /**
	 * EtherType for ARP messages.
	 */
	public static short ARP_ETHER_TYPE = 0x0806;
    static private MACAddress zeroMac = new MACAddress(new byte[6]);

    public ARPMessage(boolean isRequest, MACAddress senderMac, IPAddress senderIp, MACAddress targetMac, IPAddress targetIp) {
        this.isRequest = isRequest;
        this.senderMac = senderMac;
        this.targetMac = targetMac;
        this.senderIp = senderIp;
        this.targetIp = targetIp;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public MACAddress getSenderMac() {
        return senderMac;
    }

    public MACAddress getTargetMac() {
        return targetMac;
    }

    public IPAddress getSenderIp() {
        return senderIp;
    }

    public IPAddress getTargetIp() {
        return targetIp;
    }

    byte[] toBytes() {
        byte[] res = new byte[28];
        res[0] = 0x00; // Hardware type: Ethernet
        res[1] = 0x01;
        res[2] = 0x08; // Protocol type: IPv4
        res[3] = 0x00;
        res[4] = 0x06; // Hardware size: 6
        res[5] = 0x04; // Protocol size: 4
        res[6] = 0x00; // Opcode: request (0x0001) or reply (0x0002)
        res[7] = isRequest ? (byte) 0x01 : (byte) 0x02; // Opcode: request (0x0001) or reply (0x000 2)
        // Sender MAC address
        System.arraycopy(senderMac.getBytes(), 0, res, 8, 6);
        // Sender IP address
        byte[] senderIpBytes = senderIp.getBytes();
        System.arraycopy(senderIpBytes, 0, res, 14, 4);
        // Target MAC address
        System.arraycopy(targetMac.getBytes(), 0, res, 18, 6);
        // Target IP address
        byte[] targetIpBytes = targetIp.getBytes();
        System.arraycopy(targetIpBytes, 0, res, 24, 4);

        return res;
    }

    public static boolean isARPMessage(EthernetFrame frame) {
		return frame.getEtherType() == ARP_ETHER_TYPE;
	}
    
    static ARPMessage fromFrame(EthernetFrame frame) {
        byte[] data = frame.getPayload();
        boolean isRequest = data[6] == 0x00 && data[7] == 0x01;
        byte[] senderMacBytes = new byte[6];
        System.arraycopy(data, 8, senderMacBytes, 0, 6);
        MACAddress senderMac = new MACAddress(senderMacBytes);
        byte[] senderIpBytes = new byte[4];
        System.arraycopy(data, 14, senderIpBytes, 0, 4);
        IPAddress senderIp = new IPAddress(senderIpBytes);
        byte[] targetMacBytes = new byte[6];
        System.arraycopy(data, 18, targetMacBytes, 0, 6);
        MACAddress targetMac = new MACAddress(targetMacBytes);
        byte[] targetIpBytes = new byte[4];
        System.arraycopy(data, 24, targetIpBytes, 0, 4);
        IPAddress targetIp = new IPAddress(targetIpBytes);

        return new ARPMessage(isRequest, senderMac, senderIp, targetMac, targetIp);
    }

    /**
     * A Gratuitous ARP message (GARP) is used to announce the presence of a host on the network.
     * @param senderMac
     * @param senderIp``
     * @return
     */
    static ARPMessage createGarpRequest(MACAddress senderMac,  IPAddress senderIp) {
        return new ARPMessage(true, senderMac, senderIp, zeroMac, senderIp);
    }
}