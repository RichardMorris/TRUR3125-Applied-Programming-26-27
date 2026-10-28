package org.singsurf.jns3.generic_protocol;

/**
 * A converter between protocol container types.
 * @param <L> Lower level protocol datatype
 * @param <H> Higher level protocol data type
 */
public interface ProtocolConverter<L extends ProtocolContainer, H extends ProtocolContainer> {

	/**
	 * Extract the higher level type from the lower level type.
	 * It is assumed that this can be done without error.
	 * @param type lower level container
	 * @return high level type
	 */
	public H extract(L type);

	/**
	 * Encapsulate the higher level type in a lower level container.
	 * @param type higher level type to encapsulate.
	 * @return lower level container.
	 */
	public L encapsulate(H type);
}
