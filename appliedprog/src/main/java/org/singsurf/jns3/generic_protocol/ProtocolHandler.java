package org.singsurf.jns3.generic_protocol;

/**
 * Defines methods that attempt to handle containers
 * from a (lower level) protocol. These handlers
 * are typically called by a {@link ProtocolDelegator}.
 * 
 * @param <T> The container type.
 */
public interface ProtocolHandler<T extends ProtocolContainer> {
	/**
	 * Attempt to handle the message.
	 * @param type the container type.
	 * @return one of HANDLED, IGNORED or Invalid.
	 */
	public HandlerStatus handle(T type);
	
}
