package org.singsurf.jns3.network_layer.icmp;

public class PingReply extends ICMPMessage {
	public PingReply(int rest) {
		super((byte) 0x00, (byte) 0x00, rest);
	}

	public short getIdentifier() {
		return getRestLow();
	}
	
	public short getSequenceNumber() {
		return getRestHigh();
	}

}
