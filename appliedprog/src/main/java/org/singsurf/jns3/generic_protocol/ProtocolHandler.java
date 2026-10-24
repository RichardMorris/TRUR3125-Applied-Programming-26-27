package org.singsurf.jns3.generic_protocol;


public interface ProtocolHandler<T extends ProtocolType> {
	public static final Handled HANDLED = new Handled();
	public static final Status IGNORED = new Ignored();
	
	public static abstract sealed class Status permits Handled, Ignored, Invalid {	}
	public static final class Handled extends Status {	};
	public static final class Ignored extends Status {	};
	public static final class Invalid extends Status {	
		String reason;
		public Invalid(String reason) {
			this.reason = reason;
		}
	}
	
	public Status handle(T protocolType, ProtocolDelegator<T> delegator);
	
}
