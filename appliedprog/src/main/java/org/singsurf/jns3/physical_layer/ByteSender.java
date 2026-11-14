package org.singsurf.jns3.physical_layer;

/**
 * Sends arrays of bytes to a connection.
 */
public interface ByteSender {
	public void send(byte[] frame);
}
