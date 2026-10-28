package org.singsurf.jns3.generic_protocol;

/**
 * Defines method that receive a out-bound messages
 * @param <T> container type for this protocol.
 */
public interface ProtocolSender<T extends ProtocolContainer> {

	/**
	 * Sends a message down the pipeline
	 * @param container container of the message
	 */
	public void send(T container);

}
