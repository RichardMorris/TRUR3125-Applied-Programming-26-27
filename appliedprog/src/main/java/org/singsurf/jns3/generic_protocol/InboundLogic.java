package org.singsurf.jns3.generic_protocol;

/**
 * Interface for the in-bound logic once a type has been extracted.
 * @param <A> The type of the lower level protocol that is being handled.
 * @param <B> The type of the protocol that is being handled.
 */
public interface InboundLogic<A extends ProtocolType,B extends ProtocolType> {

	/**
	 * Process an inbound message.
	 * @param container the lower level container
	 * @param extracted the extracted higher level message with its headers.
	 */
	public void process(A container, B extracted);

}
