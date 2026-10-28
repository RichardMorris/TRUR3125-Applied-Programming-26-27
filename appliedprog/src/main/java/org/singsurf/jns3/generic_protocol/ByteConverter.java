package org.singsurf.jns3.generic_protocol;

/**
 * A converter between a protocol container type and a byte array.
 * @param <H> Higher level protocol data type
 */
public interface ByteConverter<H extends ProtocolContainer> {

	/**
	 * Create the higher level type from the bytes.
	 * It is assumed that this can be done without error.
	 * @param bytes the bytes to convert
	 * @return high level type
	 */
	public H fromBytes(byte[] bytes);

	/**
	 * Extract the bytes from a ProtocolContainer
	 * @param type higher level type.
	 * @return the bytes.
	 */
	public byte[] toBytes(H type);
}
