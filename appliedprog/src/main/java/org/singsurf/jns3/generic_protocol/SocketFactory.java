package org.singsurf.jns3.generic_protocol;

/**
 * A abstract factory for creating {@link PolledSocket} instances.
 * @param <T> data carrier for the protocol.
 */
public interface SocketFactory<T extends ProtocolContainer> {
	
	ProtocolSocket<T> createSocket(String name); 

	ProtocolSocket<T> createSocket(String name, ProtocolReceiver<T> receiver);
}
