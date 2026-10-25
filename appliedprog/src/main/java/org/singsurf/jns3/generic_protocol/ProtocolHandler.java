package org.singsurf.jns3.generic_protocol;


public interface ProtocolHandler<T extends ProtocolType> {
	public static final Handled HANDLED = new Handled();
	public static final Status IGNORED = new Ignored();
	
	public static sealed interface Status permits Handled, Ignored, Invalid {}
	public static final class Handled implements Status {	};
	public static final class Ignored implements Status {	};
	public static record Invalid(String reason) implements Status {};

	public Status handle(T type);
	
}
