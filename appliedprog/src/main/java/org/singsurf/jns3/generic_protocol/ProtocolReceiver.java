package org.singsurf.jns3.generic_protocol;

/**
 * Defines method that receive a in-bound message for this protocol
 * @param <T> container type for this protocol.
 */
public interface ProtocolReceiver<T extends ProtocolContainer> {
	/**
	 * Receive the message.
	 * @param container the container of the message 
	 */
	public void receive(T container);

}
