package org.singsurf.jns3.generic_protocol;


public interface ProtocolHandler<T extends ProtocolType> {
	
	public static abstract class Status<T> {	}
	public static final class Handled<T> extends Status<T> {	}
	public static final class Ignored<T> extends Status<T> {	}
	public static final class Invalid<T> extends Status<T> {	
		String reason;
		public Invalid(String reason) {
			this.reason = reason;
		}
	}
	public static final class Modifies<T> extends Status<T> {
		T modifiedMessage;
		public Modifies(T modifiedMessage) {
			this.modifiedMessage = modifiedMessage;
		}
		T getModifiedMessage() {
			return modifiedMessage;
		}
	}

	
	public Status<T> handle(T protocolType);
	
	public static<S extends ProtocolType> Status<S> Ignored() { 
		return new Ignored<S>();
	}

	public static<S extends ProtocolType> Status<S> Handled() { 
		return new Handled<S>();
	}

	public static<S extends ProtocolType> Status<S> Invalid(String reason) { 
		return new Invalid<S>(reason);
	}

}
