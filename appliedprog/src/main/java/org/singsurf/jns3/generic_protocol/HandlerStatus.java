package org.singsurf.jns3.generic_protocol;

/**
 * The return type of the protocol.
 */
public sealed class HandlerStatus permits HandlerStatus.Handled, HandlerStatus.Ignored, HandlerStatus.Invalid {

	/**
	 * Indicates the container has been handled completely
	 * and no further processing is needed.
	 */
	static final class Handled extends HandlerStatus {	}

	/**
	 * Indicated that the handler could not handle the 
	 * container and it should be passed to subsequent handlers.
	 */
	static final class Ignored extends HandlerStatus {	}

	/**
	 * Indicates the container has been handled completely
	 * and no further processing is needed.
	 */
	public static final Handled COMPLETED = new Handled();
	/**
	 * Indicated that the handler could not handle the 
	 * container and it should be passed to subsequent handlers.
	 */
	public static HandlerStatus CONTINUE = new Ignored();
	/**
	 * Indicates that the container containes invalid data,
	 * causes an error to be logged and processing should cease.
	 */
	public static final class Invalid extends HandlerStatus {
		public final String reason;
		public Invalid(String reason) {
			this.reason = reason;
		}
		public String reason() {
			return reason;
		}
	}
}
	