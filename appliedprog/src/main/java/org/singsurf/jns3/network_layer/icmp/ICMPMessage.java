package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.OnesComplementNumber;

public class ICMPMessage {
	byte type;
	byte code;
	short checksum;
	int restOfHeader; // 4 bytes, can be used for different purposes depending on the type and code
	byte[] data; // variable length data
	
	public ICMPMessage(byte type, byte code, int restOfHeader, byte[] data) {
		this.type = type;
		this.code = code;
		this.checksum = 0;
		this.restOfHeader = restOfHeader;
		this.data = data;
		this.checksum = calculateChecksum().getComplement().getValue();
	}

	public ICMPMessage(byte type, byte code, short restLow, short restHigh) {
		this(type, code, ((restHigh & 0xFFFF) << 16) | (restLow & 0xFFFF), new byte[0]);
	}

	private ICMPMessage(byte type, byte code, short checksum, short restLow, short restHigh, byte[] data) {
		this.type = type;
		this.code = code;
		this.checksum = checksum;
		this.restOfHeader = ((restHigh & 0xFFFF) << 16) | (restLow & 0xFFFF);
		this.data = data;
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

	public static ICMPMessage fromPacket(IPPacket packet) {
		if(packet.getProtocol() != 1 ) {
			throw new IllegalArgumentException("Not an ICMP packet");
		}
		var data = packet.getData();
		if(data.length < 8) {
			return ICMPErrorMessage.tooShort(packet);
		}
		byte type = data[0];
		byte code = data[1];
		short checksum = (short) ((data[2] & 0xFF) | ((data[3] & 0xFF) << 8) );
		short restLow = (short) ((data[4] & 0xFF) | ((data[5] & 0xFF) << 8));
		short restHigh = (short) ((data[6] & 0xFF) | ((data[7] & 0xFF) << 8));
		byte[] icmpData = new byte[data.length - 8];
		System.arraycopy(data, 8, icmpData, 0, icmpData.length);
		var res = new ICMPMessage(type, code, checksum, restLow, restHigh, icmpData);
		if(!res.calculateChecksum().equals(OnesComplementNumber.MINUS_ZERO)) {
			return ICMPErrorMessage.badChecksum(packet);
		}
		return res;
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
	
	public short getRestLow() {
		return (short) (restOfHeader & 0xFFFF);
	}
	
	public short getRestHigh() {
		return (short) ((restOfHeader >> 16) & 0xFFFF);
	}
	
	public byte[] getData() {
		return data;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[8 + data.length];
		bytes[0] = type;
		bytes[1] = code;
		bytes[2] = (byte) (checksum & 0xFF);
		bytes[3] = (byte) (checksum >> 8);
		bytes[4] = (byte) (restOfHeader & 0xFF);
		bytes[5] = (byte) ((restOfHeader >> 8) & 0xFF);
		bytes[6] = (byte) ((restOfHeader >> 16) & 0xFF);
		bytes[7] = (byte) ((restOfHeader >> 24) & 0xFF);
		System.arraycopy(data, 0, bytes, 8, data.length);
		return bytes;
	}
	
}
