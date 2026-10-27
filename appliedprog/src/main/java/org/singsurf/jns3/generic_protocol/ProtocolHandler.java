package org.singsurf.jns3.generic_protocol;

/**
 * Defines methods that attempt to handle containers
 * from a (lower level) protocol. These handlers
 * are typically called by a {@link ProtocolDelegator}.
 * 
 * @param <T> The container type.
 */
public interface ProtocolHandler<T extends ProtocolType> {
	/**
	 * Indicates the container has been handled completely
	 * and no further processing is needed.
	 */
	public static final Handled HANDLED = new Handled();
	/**
	 * Indicated that the handler could not handle the 
	 * container and it should be passed to subsequent handlers.
	 */
	public static final Status IGNORED = new Ignored();
	
	/**
	 * The return type of the protocol.
	 */
	public static sealed interface Status permits Handled, Ignored, Invalid {}
	/**
	 * Indicates the container has been handled completely
	 * and no further processing is needed.
	 */
	static final class Handled implements Status {	};
	/**
	 * Indicated that the handler could not handle the 
	 * container and it should be passed to subsequent handlers.
	 */
	static final class Ignored implements Status {	};
	/**
	 * Indicates that the container containes invalid data,
	 * causes an error to be logged and processing should cease.
	 */
	public static record Invalid(String reason) implements Status {};

	/**
	 * Attempt to handle the message.
	 * @param type the container type.
	 * @return one of HANDLED, IGNORED or Invalid.
	 */
	public Status handle(T type);
	
}
