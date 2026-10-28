package org.singsurf.jns3.generic_protocol;

/**
 * A converter between protocol container types.
 * @param <L> Lower level protocol datatype
 * @param <H> Higher level protocol data type
 */
public interface ProtocolExtractor<L extends ProtocolContainer, H extends ProtocolContainer> {

	/**
	 * Extract the higher level type from the lower level type.
	 * It is assumed that this can be done without error.
	 * @param type lower level container
	 * @return high level type
	 */
	public H extract(L type);
	
	/**
	 * Check if the lower level type can be extracted to the higher level type.
	 * @param type lower level container
	 * @return true if extractable, false otherwise
	 */
	public boolean isExtractable(L type);
}
