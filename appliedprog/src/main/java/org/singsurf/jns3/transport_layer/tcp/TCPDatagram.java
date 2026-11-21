package org.singsurf.jns3.transport_layer.tcp;

import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

public class TCPDatagram implements ProtocolContainer {
	public static final byte FLAG_FIN = 0x01;
	public static final byte FLAG_SYN = 0x02;
	public static final byte FLAG_RST = 0x04;
	public static final byte FLAG_PSH = 0x08;
	public static final byte FLAG_ACK = 0x10;
	public static final byte FLAG_URG = 0x20;
	public static final byte FLAG_ECE = 0x40;
	public static final byte FLAG_CWR = (byte) 0x80;
	
	short sourcePort;
	short destinationPort;
	int sequenceNumber;
	int acknowledgementNumber;
	byte dataOffset = 0x50; // 4 bits data offset, 4 bits reserved
	byte flags; // 8 bits flags
	short windowSize;
	short checksum;
	short urgentPointer;
	
	byte[] contents;

	PseudoHeader psudoHeader;
	
	/**
	 * A pseudo header contained additional information from the enclosing packet.
	 * It is used to calculate the checksum of the UDP datagram and is not transmitted.
	 * <ul>
	 * <li>Source IP Address: 4 bytes
	 * <li>Destination IP Address: 4 bytes
	 * <li>Protocol: 1 byte
	 * <li>Zeros: 1 byte
	 * <li>UDP Length: 2 bytes
	 * </ul>
	 */
	public record PseudoHeader(
			IPAddress sourceIP, 
			IPAddress destinationIP, 
			byte protocol,
			short length) { }


	/**
	 * Constructor used by applications for creating the initial TCP datagram.
	 * Destination IP can be set via the pseudo header.
	 * 
	 * @param sourcePort
	 * @param destinationPort
	 * @param contents
	 * @param psudoHeader used for calculating the checksum, not transmitted
	 */
	public TCPDatagram(short sourcePort, short destinationPort, 
			byte[] contents, PseudoHeader psudoHeader) {
		super();
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.sequenceNumber = 0;
		this.acknowledgementNumber = 0;
		// 4 bits data offset, 4 bits reserved
		this.flags = 0x00;
		this.windowSize = 0;
		this.checksum = 0;
		this.contents = contents;
		this.psudoHeader = psudoHeader;
		this.checksum = calculateHeaderChecksum().getComplement().getValue();
	}

	private TCPDatagram(byte[] bytes, PseudoHeader psudoHeader) {
		this.sourcePort = (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
		this.destinationPort = (short) ((bytes[2] << 8) | (bytes[3] & 0xFF));
		this.sequenceNumber = 
				((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) | ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
		this.acknowledgementNumber = 
				((bytes[8] & 0xFF) << 24) | ((bytes[9] & 0xFF) << 16) | ((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF);
		// 4 bits data offset, 4 bits reserved
		this.flags = bytes[13];
		this.windowSize = (short) ((bytes[14] << 8) | (bytes[15] & 0xFF));
		this.checksum = (short) ((bytes[16] << 8) | (bytes[17] & 0xFF));
		this.contents = new byte[bytes.length - 20];
		System.arraycopy(bytes, 20, this.contents, 0, this.contents.length);
		this.psudoHeader = psudoHeader;
	}
	

	@Override
	public byte[] getBytes() {
		byte[] datagram = new byte[contents.length + 20];
		datagram[0] = (byte) (sourcePort >> 8);
		datagram[1] = (byte) (sourcePort & 0xFF);
		datagram[2] = (byte) (destinationPort >> 8);
		datagram[3] = (byte) (destinationPort & 0xFF);
		datagram[4] = (byte) (sequenceNumber >> 24);
		datagram[5] = (byte) ((sequenceNumber >> 16) & 0xFF);
		datagram[6] = (byte) ((sequenceNumber >> 8) & 0xFF);
		datagram[7] = (byte) (sequenceNumber & 0xFF);
		datagram[8] = (byte) (acknowledgementNumber >> 24);
		datagram[9] = (byte) ((acknowledgementNumber >> 16) & 0xFF);
		datagram[10] = (byte) ((acknowledgementNumber >> 8) & 0xFF);
		datagram[11] = (byte) (acknowledgementNumber & 0xFF);
		datagram[12] = dataOffset;
		datagram[13] = flags;
		datagram[14] = (byte) (windowSize >> 8);
		datagram[15] = (byte) (windowSize & 0xFF);
		datagram[16] = (byte) (checksum >> 8);
		datagram[17] = (byte) (checksum & 0xFF);
		datagram[18] = (byte) (urgentPointer >> 8);
		datagram[19] = (byte) (urgentPointer & 0xFF);
		System.arraycopy(contents, 0, datagram, 20, contents.length);
		return datagram;
	}

	@Override
	public byte[] getContents() {
		return contents;
	}

	@Override
	public boolean isValid() {
		return calculateHeaderChecksum().equals(OnesComplementNumber.MINUS_ZERO);
	}
	
	public OnesComplementNumber calculateHeaderChecksum() {
        var sum = new OnesComplementNumber((byte) 0, psudoHeader.protocol);

        sum = sum.add(psudoHeader.sourceIP.getHighWord());
        sum = sum.add(psudoHeader.sourceIP.getLowWord());
        sum = sum.add(psudoHeader.destinationIP.getHighWord());
        sum = sum.add(psudoHeader.destinationIP.getLowWord());
        sum = sum.add(psudoHeader.protocol, (byte) 0);
        sum = sum.add(psudoHeader.length); 
        sum = sum.add(sourcePort);
        sum = sum.add(destinationPort);
        sum = sum.add(sequenceNumber >> 16);
        sum = sum.add(sequenceNumber & 0xFFFF);
        sum = sum.add(acknowledgementNumber >> 16);
        sum = sum.add(acknowledgementNumber & 0xFFFF);
        sum = sum.add(dataOffset << 8 | flags);
        sum = sum.add(windowSize);
        sum = sum.add(checksum);
        sum = sum.add(urgentPointer);
        sum.add(contents);

        return sum;
    }


	public String flagsToString() {
		StringBuilder sb = new StringBuilder();
		if((flags & FLAG_FIN) != 0) sb.append("FIN ");
		if((flags & FLAG_SYN) != 0) sb.append("SYN ");
		if((flags & FLAG_RST) != 0) sb.append("RST ");
		if((flags & FLAG_PSH) != 0) sb.append("PSH ");
		if((flags & FLAG_ACK) != 0) sb.append("ACK ");
		if((flags & FLAG_URG) != 0) sb.append("URG ");
		if((flags & FLAG_ECE) != 0) sb.append("ECE ");
		if((flags & FLAG_CWR) != 0) sb.append("CWR ");
		if(sb.length() == 0) sb.append("NONE");
		return sb.toString().trim().replace(' ','-');
	}
	
	@Override
	public String toString() {
		return "TCPDatagram[srcPort=" + sourcePort + ", destPort=" + destinationPort
				+ ", seqNum=" + sequenceNumber + ", ackNum=" + acknowledgementNumber
				+ ", DO=" + String.format("0x%02X", dataOffset) + ", "
				+ "flags=" + flagsToString() + ", WS=" + windowSize 
				+ ", checksum=" + String.format("0x%04X", checksum)
				+ ", UP=" + urgentPointer 
				+ ", contents=" + contents.length + "]";
	}
	@Override
	public String toHeaderString() {
		return toString();
	}

	
	// Getters
	
	public short getSourcePort() {
		return sourcePort;
	}
	
	public short getDestinationPort() {
		return destinationPort;
	}
	
	public short getChecksum() {
		return checksum;
	}

	public PseudoHeader getPsudoHeader() {
		return this.psudoHeader;
	}

	public void setSYN(boolean b) {
		if(b) {
			flags |= FLAG_SYN;
		} else {
			flags &= ~FLAG_SYN;
		}
		recalculateHeaderChecksum();
	}

	public void setACK(boolean b) {
		if(b) {
			flags |= FLAG_ACK;
		} else {
			flags &= ~FLAG_ACK;
		}
		recalculateHeaderChecksum();
	}

	public void setFIN(boolean b) {
		if(b) {
			flags |= FLAG_FIN;
		} else {
			flags &= ~FLAG_FIN;
		}
		recalculateHeaderChecksum();
	}
	
	public void setRST(boolean b) {
		if(b) {
			flags |= FLAG_RST;
		} else {
			flags &= ~FLAG_RST;
		}
		recalculateHeaderChecksum();
	}
	
	public void setPSH(boolean b) {
		if(b) {
			flags |= FLAG_PSH;
		} else {
			flags &= ~FLAG_PSH;
		}
		recalculateHeaderChecksum();
	}

	public boolean isSYN() {
		return (flags & FLAG_SYN) != 0;
	}
	
	public boolean isACK() {
		return (flags & FLAG_ACK) != 0;
	}
	
	public boolean isFIN() {
		return (flags & FLAG_FIN) != 0;
	}
	
	public boolean isRST() {
		return (flags & FLAG_RST) != 0;
	}
	
	public boolean isPSH() {
		return (flags & FLAG_PSH) != 0;
	}

	private void recalculateHeaderChecksum() {
		checksum = 0;
		checksum = calculateHeaderChecksum().getComplement().getValue();
	}

	public static TCPDatagram fromPacket(IPPacket packet) {
		var psudo = new PseudoHeader(packet.getSourceAddress(), 
				packet.getDestinationAddress(), IPPacket.TCP,
				(short) packet.getContents().length);
		var bytes = packet.getContents();
		return new TCPDatagram(bytes, psudo);
	}

}
