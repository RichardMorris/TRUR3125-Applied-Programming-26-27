package org.singsurf.jns3.network_layer.icmp;

public class PingRequest extends ICMPMessage {
	public PingRequest(short identifier, short seqenceNumber) {
		super((byte) 0x08, (byte) 0x00, identifier, seqenceNumber);
	}

	public short getIdentifier() {
		return getRestLow();
	}
	
	public short getSequenceNumber() {
		return getRestHigh();
	}
}
