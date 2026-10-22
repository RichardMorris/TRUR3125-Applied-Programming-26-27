package org.singsurf.jns3.generic_protocol;

public interface ProtocolHandler<T extends ProtocolType> {

	public boolean matches(T protocolType);
	public T handle(T protocolType);
}
