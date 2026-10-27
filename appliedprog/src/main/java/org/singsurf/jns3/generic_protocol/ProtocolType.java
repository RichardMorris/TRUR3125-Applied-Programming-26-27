package org.singsurf.jns3.generic_protocol;

/**
 * Type of data for this protocol.
 */
public interface ProtocolType {
	/**
	 * Get the full byte representation, including header and data section
	 * @return an array of bytes matching specification of protocol
	 */
	public byte[] getBytes();
	/**
	 * Gets the byte representation of the data section
	 * @return an array of bytes
	 */
	public byte[] getContents();
	/**
	 * Returns if the container is well formed with valid checksums.
	 * @return true if it passes all check, false otherwise. 
	 */
	public boolean isValid();
}
