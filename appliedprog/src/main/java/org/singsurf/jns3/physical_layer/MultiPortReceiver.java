package org.singsurf.jns3.physical_layer;

/**
 * Receives frames from multiple ports.
 */
public interface MultiPortReceiver {
	
	public void receiveFrame(int portNumber, byte[] frame);

}