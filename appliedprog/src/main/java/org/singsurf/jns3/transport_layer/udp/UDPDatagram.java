package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

/**
 * Represents a UDP datagram.
 * <p>
 * They are encapsulated in IP packets with protocol number is 0x11 (17),
 * It is connection less, unreliable, not ordered, and fast.
 * <p>
 *  A UDPDatagram header contains:
 * <ul>
 * <li>Source Port: 2 bytes
 * <li>Destination Port: 2 bytes
 * <li>Length: 2 bytes
 * <li> Checksum: 2 bytes, unused
 * </ul>
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/User_Datagram_Protocol">UDP</a> in Wikipedia
 * and <a href="https://www.rfc-editor.org/info/rfc768/">RFC 768</a>.
 **/
public class UDPDatagram implements ProtocolContainer {
	short sourcePort;
	short destinationPort;
	short length;
	short checksum;
	byte[] contents;
	PseudoHeader psudoHeader;
	
	/**
	 * A pseudo header contained additional information from the enclosing packet.
	 * It is used to calculate the checksum of the UDP datagram and is not transmitted.
	 * <ul>
	 * <li>Source IP Address: 4 bytes
	 * <li>Destination IP Address: 4 bytes
	 * <li>Protocol: 1 byte
	 * </ul>
	 */
	public record PseudoHeader(
			IPAddress sourceIP, 
			IPAddress destinationIP, 
			byte protocol) { }
	
	/**
	 * Simple constructor used by applications for creating the initial UDP datagram,
	 * with the pseudo header set to zero addresses and a zero checksum.
	 * @param sourcePort
	 * @param destinationPort
	 * @param contents
	 */
	public UDPDatagram(short sourcePort, short destinationPort, byte[] contents) {
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.length = (short) (contents.length+ 8); // 8 bytes for the UDP header
		this.checksum = 0;
		this.contents = contents;
		this.psudoHeader = new PseudoHeader(IPAddress.ZERO_ADDRESS, IPAddress.ZERO_ADDRESS, IPPacket.UDP);
//		this.checksum = calculateHeaderChecksum().getComplement().getValue();
	}

	/**
	 * Constructor with a pseudo-header
	 * @param srcPort source port
	 * @param destPort destination port
	 * @param contents payload
	 * @param psudoHeader used for calculating the checksum, not transmitted
	 */
	public UDPDatagram(short srcPort, short destPort, byte[] contents, PseudoHeader psudoHeader) {
		this.sourcePort = srcPort;
		this.destinationPort = destPort;
		this.length = (short) (contents.length+ 8); // 8 bytes for the UDP header
		this.checksum = 0;
		this.contents = contents;
		this.psudoHeader = psudoHeader;
		this.checksum = calculateHeaderChecksum().getComplement().getValue();
	}

	/**
	 * Uses the provided checksum
	 */
	public UDPDatagram(short sourcePort, short destinationPort, short check, byte[] contents, PseudoHeader psudoHeader) {
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.length = (short) (contents.length+ 8); // 8 bytes for the UDP header
		this.checksum = check;
		this.contents = contents;
		this.psudoHeader = psudoHeader;
	}

	public short getSourcePort() {
		return sourcePort;
	}
	
	public short getDestinationPort() {
		return destinationPort;
	}
	
	public short getLength() {
		return length;
	}
	
	public short getChecksum() {
		return checksum;
	}
	
	public byte[] getContents() {
		return contents;
	}
	
	public byte[] getBytes() {
		byte[] datagram = new byte[length];
		datagram[0] = (byte) (sourcePort >> 8);
		datagram[1] = (byte) (sourcePort & 0xFF);
		datagram[2] = (byte) (destinationPort >> 8);
		datagram[3] = (byte) (destinationPort & 0xFF);
		datagram[4] = (byte) (length >> 8);
		datagram[5] = (byte) (length & 0xFF);
		datagram[6] = (byte) (checksum >> 8);
		datagram[7] = (byte) (checksum & 0xFF);
		System.arraycopy(contents, 0, datagram, 8, contents.length);
		return datagram;
	}
	
	static UDPDatagram fromBytes(byte[] datagram, PseudoHeader psudo) {
		short sourcePort = (short) ((datagram[0] << 8) | (datagram[1] & 0xFF));
		short destinationPort = (short) ((datagram[2] << 8) | (datagram[3] & 0xFF));
		short length = (short) ((datagram[4] << 8) | (datagram[5] & 0xFF));
//		short checksum = (short) ((datagram[6] << 8) | (datagram[7] & 0xFF));
		byte[] contents = new byte[length - 8];
		System.arraycopy(datagram, 8, contents, 0, contents.length);
		return new UDPDatagram(sourcePort, destinationPort, contents,psudo);
	}

	public String toString() {
		return "UDPDatagram [sourcePort=" + sourcePort + ", destinationPort=" + destinationPort + ", length=" + length
				+ ", checksum=" + checksum + ", contents=" + new String(contents) + "]";
	}

	public OnesComplementNumber calculateHeaderChecksum() {
        var sum = new OnesComplementNumber((byte) 0, psudoHeader.protocol);

        sum = sum.add(psudoHeader.sourceIP.getHighWord());
        sum = sum.add(psudoHeader.sourceIP.getLowWord());
        sum = sum.add(psudoHeader.destinationIP.getHighWord());
        sum = sum.add(psudoHeader.destinationIP.getLowWord());
        sum = sum.add(psudoHeader.protocol, (byte) 0);
        sum = sum.add(length); 
        sum = sum.add(sourcePort);
        sum = sum.add(destinationPort);
        sum = sum.add(length); 
        sum = sum.add(checksum);
        sum.add(contents);

        return sum;
    }

	@Override
	public boolean isValid() {
		return calculateHeaderChecksum().equals(OnesComplementNumber.MINUS_ZERO);
	}

	public PseudoHeader getPsudoHeader() {
		return this.psudoHeader;
	}

	public String toHeaderString() {
		return "UDPDatagram[srcPort=" + sourcePort + ", destPort=" + destinationPort + ", len=" + length
			+ ", check=" + String.format("0x%04X", checksum) + "]";
	}

	/**
	 * Converts this UDP datagram into an IP packet with the given source and destination IP addresses and TTL.
	 * The psudo header is updated with the provided source and destination IP addresses
	 * and the checksum computed.
	 *
	 * @param srcIP the source IP address
	 * @param destIP the destination IP address
	 * @param ttl the time-to-live value for the IP packet
	 * @return an IPPacket containing this UDP datagram
	 */
	public IPPacket toIPPacket(IPAddress srcIP, IPAddress destIP, int ttl) {
		this.psudoHeader = new PseudoHeader(srcIP, destIP, IPPacket.UDP);
		this.checksum = 0;
		this.checksum = calculateHeaderChecksum().getComplement().getValue();
		return new IPPacket(IPPacket.UDP, ttl, srcIP, destIP, this.getBytes());
	}
	
	
}
