package org.singsurf.jns3.generic_protocol;

/**
 * A abstract factory for creating {@link ProtocolSocket} instances.
 * @param <T> data carrier for the protocol.
 */
public interface SocketFactory<T> {
	
	ProtocolSocket<T> createSocket(String name);

}
