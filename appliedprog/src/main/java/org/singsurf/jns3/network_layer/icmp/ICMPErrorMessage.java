package org.singsurf.jns3.network_layer.icmp;

import org.singsurf.jns3.network_layer.IPPacket;

public class ICMPErrorMessage extends ICMPMessage {

	ICMPErrorMessage(byte type, byte code, int rest, byte[] data) {
		super(type, code, rest, data);
	}

	public static ICMPErrorMessage createMessage(byte type, byte code, IPPacket packet) {
		var data = packet.getData();
		if (data.length < 8) {
		}
		short restLow = (short) ((data[4] & 0xFF) | ((data[5] & 0xFF) << 8));
		short restHigh = (short) ((data[6] & 0xFF) | ((data[7] & 0xFF) << 8));
		byte[] newData = new byte[data.length - 8];
		System.arraycopy(data, 8, newData, 0, newData.length);
		return new ICMPErrorMessage(
				type, code, ((restHigh & 0xFFFF) << 16) | (restLow & 0xFFFF), 
				newData);
	}

	public static ICMPMessage tooShort(IPPacket packet) {
		return new ICMPErrorMessage((byte) 12, (byte) 2, 0, new byte[0]);
	}

	public static ICMPMessage badChecksum(IPPacket packet) {
		return new ICMPErrorMessage((byte) 12, (byte) 2, 0, packet.getData());
	}

	public static ICMPErrorMessage createTTLExceeded(IPPacket packet) {
		return createMessage((byte) 11, (byte) 0, packet);
	}

}
