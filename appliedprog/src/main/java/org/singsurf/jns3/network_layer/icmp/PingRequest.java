package org.singsurf.jns3.network_layer.icmp;

public class PingRequest extends ICMPMessage {
	public PingRequest(short identifier, short seqenceNumber) {
		super((byte) ICMPMessage.PING_REQUEST_TYPE, (byte) 0x00, identifier, seqenceNumber);
	}

	public short getIdentifier() {
		return getRestLow();
	}
	
	public short getSequenceNumber() {
		return getRestHigh();
	}
}
