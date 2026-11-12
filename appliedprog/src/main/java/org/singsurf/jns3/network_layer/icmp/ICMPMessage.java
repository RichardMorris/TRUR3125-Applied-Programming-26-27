package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

/**
 * An The  Internet Control Message Protocol (ICMP) message
 * it is encapsulate in an `IPPackage` with protocol 0x01.
 * It has an 8-byte header:
 * <ul>
  * <li>1 byte for the type of the ICMP message.
  * <li>1 byte for the sub-type (code) of the ICMP message.
  * <li>2 byte One's complement checksum of headers and data.
  * <li>4 bytes for the 'rest' of the header, this can 
  * be split into two 2-byte fields, or used as a single 4-byte field,
  * <li> It had a data section with a maximum of 579 bytes.
 *</ul>
 *
 * See <a href="../../../../../../../Week11.md">Week 11</a>.
 */
public class ICMPMessage implements ProtocolContainer {
	private static final byte TOO_SHORT_CODE = (byte) 2;
	private static final byte BAD_CHECKSUM_CODE = (byte) 0;
	static final byte PING_REPLY_TYPE = 0;
	static final byte PING_REQUEST_TYPE = 8;
	static final byte TIME_EXCEEDED = 11;
	static final byte TTL_EXCEEDED_CODE = 0;
	static final byte PARAMETER_PROBLEM = 12;
	
	public record PsudoHeader(IPAddress sourceIP, IPAddress destIP, 
			byte protocol, short length, byte ttl) { }

	byte type;
	byte code;
	short checksum;
	int restOfHeader; // 4 bytes, can be used for different purposes depending on the type and code
	byte[] data; // variable length data
	PsudoHeader psudoHeader;
	
	public ICMPMessage(byte type, byte code, int restOfHeader, byte[] data) {
		this.type = type;
		this.code = code;
		this.checksum = 0;
		this.restOfHeader = restOfHeader;
		this.data = data;
		this.checksum = 0;
		this.checksum = calculateChecksum().getComplement().getValue();
		this.psudoHeader = new PsudoHeader(IPAddress.ZERO_ADDRESS, IPAddress.ZERO_ADDRESS, IPPacket.IMCP, (short) (data.length + 8), BAD_CHECKSUM_CODE);
	}

	public ICMPMessage(byte type, byte code, short id, short seqNo) {
		this(type, code, ((id & 0xFFFF) << 16) | (seqNo & 0xFFFF), new byte[0]);
	}

	private ICMPMessage(byte type, byte code, short checksum, short id, short seqNo, byte[] data, PsudoHeader psudo) {
		this.type = type;
		this.code = code;
		this.checksum = checksum;
		this.restOfHeader = ((id & 0xFFFF) << 16) | (seqNo & 0xFFFF);
		this.data = data;
		this.psudoHeader = psudo;
	}

	public ICMPMessage(byte type, byte code, int rest) {
		this(type, code, rest, new byte[0]);
	}

	public OnesComplementNumber calculateChecksum() {
        var sum = new OnesComplementNumber(type,code);
		sum = sum.add(checksum);
		sum = sum.add((restOfHeader >> 16) & 0xFFFF);
		sum = sum.add(restOfHeader & 0xFFFF);
		for (int i = 0; i < data.length; i += 2) {
			int word = (data[i] & 0xFF) << 8;
			if (i + 1 < data.length) {
				word |= (data[i + 1] & 0xFF);
			}
			sum = sum.add(word);
		}
		return sum;
	}

    public boolean isValid() {
		return calculateChecksum().equals(OnesComplementNumber.MINUS_ZERO);
	}

	public byte getType() {
		return type;
	}
	
	public byte getCode() {
		return code;
	}
	
	public short getChecksum() {
		return checksum;
	}
	
	public int getRestOfHeader() {
		return restOfHeader;
	}
	
	private short getRestLow() {
		return (short) (restOfHeader & 0xFFFF);
	}
	
	private short getRestHigh() {
		return (short) ((restOfHeader >> 16) & 0xFFFF);
	}
	
	public short getIdentifier() {
		return getRestHigh();
	}

	public short getSequenceNumber() {
		return getRestLow();
	}

	
	public byte[] getContents() {
		return data;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[8 + data.length];
		bytes[0] = type;
		bytes[1] = code;
		bytes[2] = (byte) (checksum >> 8);
		bytes[3] = (byte) (checksum & 0xFF);
		bytes[4] = (byte) ((restOfHeader >> 24) & 0xFF);
		bytes[5] = (byte) ((restOfHeader >> 16) & 0xFF);
		bytes[6] = (byte) ((restOfHeader >> 8) & 0xFF);
		bytes[7] = (byte) (restOfHeader & 0xFF);
		System.arraycopy(data, 0, bytes, 8, data.length);
		return bytes;
	}
	
	@Override
	public String toString() {
		return String.format("ICMP[%d, %d, 0x%08X, %d]", 
				type & 0xFF, code & 0xFF, restOfHeader, data != null ? data.length : 0);
	}


	public boolean isPingRequest() {
		return type == PING_REQUEST_TYPE;
	}

	public boolean isPingReply() {
		return type == PING_REPLY_TYPE;
	}

	public boolean isTTLExpired() {
		return type == TIME_EXCEEDED && code == 0;
	}

	public boolean isParemeterProblem() {
		return type == PARAMETER_PROBLEM;
	}

	static boolean isICMPMessage(IPPacket packet) {
		return packet.getProtocol() == 1;
	}

	/**
	 * Attempt to parse an ICMP message from an IP packet. 
	 * If the packet if the data is too short, or if the checksum is invalid, 
	 * an error message will be returned instead.
	 * @param packet packet containing the ICMP message
	 * @return the parsed ICMP message, or an error message if the packet is invalid
	 * @throws IllegalArgumentException if the packet is not an ICMP packet
	 */
	static ICMPMessage parsePacket(IPPacket packet) {
		if(packet.getProtocol() != 1 ) {
			throw new IllegalArgumentException("Not an ICMP packet");
		}
		var data = packet.getContents();
		if(data.length < 8) {
			return ICMPMessage.createTooShortError(packet);
		}
		byte type = data[0];
		byte code = data[1];
		short checksum = (short) ((data[2] & 0xFF) << 8 | ((data[3] & 0xFF)) );
		short restLow = (short) ((data[4] & 0xFF) << 8 | ((data[5] & 0xFF)));
		short restHigh = (short) ((data[6] & 0xFF)  << 8 | ((data[7] & 0xFF)));
		byte[] icmpData = new byte[data.length - 8];
		System.arraycopy(data, 8, icmpData, 0, icmpData.length);
		var psudo = new PsudoHeader(
				packet.getSourceAddress(), 
				packet.getDestinationAddress(), IPPacket.IMCP, 
				(short) packet.getTotalLength(), packet.getTimeToLive());
		
		var res = new ICMPMessage(type, code, checksum, restLow, restHigh, icmpData, psudo);
		if(!res.calculateChecksum().equals(OnesComplementNumber.MINUS_ZERO)) {
			return ICMPMessage.createBadChecksumError(packet);
		}
		return res;
	}


	public static ICMPMessage createTTLExpired(IPPacket packet) {
		return createMessage(TIME_EXCEEDED, TTL_EXCEEDED_CODE, packet);
	}

	public static ICMPMessage createBadChecksumError(IPPacket packet) {
		return new ICMPMessage(PARAMETER_PROBLEM, BAD_CHECKSUM_CODE, 0, packet.getContents());
	}

	public static ICMPMessage createTooShortError(IPPacket packet) {
		return new ICMPMessage(PARAMETER_PROBLEM, TOO_SHORT_CODE, 0, packet.getContents());
	}

	static ICMPMessage createMessage(byte type, byte code, IPPacket packet) {
		var data = packet.getContents();
		if (data.length < 8) {
			throw new IllegalArgumentException("Packet data too short for ICMP message");
		}
		var hlen = packet.getTotalLength() - packet.getContents().length;
		var bytes = packet.getBytes();
		byte[] newData = new byte[hlen + 8];
		System.arraycopy(bytes, 0, newData, 0, hlen + 8);
		return new ICMPMessage(
				type, code, 0, 
				newData);
	}

	public PsudoHeader getPsudoHeader() {
		return psudoHeader;
	}

}
