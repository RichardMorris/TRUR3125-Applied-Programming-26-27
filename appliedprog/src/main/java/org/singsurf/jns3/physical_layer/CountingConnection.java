package org.singsurf.jns3.physical_layer;

/**
 * Counts the number of frames that pass
 * through the connection.
 */
public class CountingConnection extends PhysicalConnection {
	int forwardCount = 0;
	int reverseCount = 0;
		
	public CountingConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
		super(dev1, dev2);
	}

	@Override
	protected void send(byte[] frame, boolean reversed) {
		if (reversed) {
			reverseCount++;
		} else {
			forwardCount++;
		}
		receive(frame, reversed);
	}

	public void reset() {
		forwardCount = 0;
		reverseCount = 0;
	}
	
	public int getForwardCount() {
		return forwardCount;
	}
	
	public int getReverseCount() {
		return reverseCount;
	}

}
