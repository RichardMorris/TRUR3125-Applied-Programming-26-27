package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.network_layer.IPPacket;

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
   <li>Length: 2 bytes
   <li> Checksum: 2 bytes, unused
  </ul>
   */
public class UDPDatagram {
	short sourcePort;
	short destinationPort;
	short length;
	short checksum;
	byte[] contents;
	
	public UDPDatagram(short sourcePort, short destinationPort, byte[] contents) {
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.length = (short) (contents.length+ 8); // 8 bytes for the UDP header
		this.checksum = 0;
		this.contents = contents;
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
	
	public byte[] toBytes() {
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
	
	private static UDPDatagram fromBytes(byte[] datagram) {
		short sourcePort = (short) ((datagram[0] << 8) | (datagram[1] & 0xFF));
		short destinationPort = (short) ((datagram[2] << 8) | (datagram[3] & 0xFF));
		short length = (short) ((datagram[4] << 8) | (datagram[5] & 0xFF));
//		short checksum = (short) ((datagram[6] << 8) | (datagram[7] & 0xFF));
		byte[] contents = new byte[length - 8];
		System.arraycopy(datagram, 8, contents, 0, contents.length);
		return new UDPDatagram(sourcePort, destinationPort, contents);
	}

	public String toString() {
		return "UDPDatagram [sourcePort=" + sourcePort + ", destinationPort=" + destinationPort + ", length=" + length
				+ ", checksum=" + checksum + ", contents=" + new String(contents) + "]";
	}

	public static boolean isUDPDatagram(IPPacket packet) {
		byte protocol = packet.getProtocol();
		return protocol == IPPacket.UDP; // 17 is the protocol number for UDP
	}
	
	public static UDPDatagram fromPacket(IPPacket packet) {
		return fromBytes(packet.getData());
	}
	
	
}
