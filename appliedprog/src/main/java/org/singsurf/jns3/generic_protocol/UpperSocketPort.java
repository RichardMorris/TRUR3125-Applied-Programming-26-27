package org.singsurf.jns3.generic_protocol;

/**
 * A port on the upper side of a protocols using sockets
 * allowing application and higher level protocols to register
 * <p>
 * Sockets are created by a {@link SocketFactory} and registered with the port.
 * 
 * @param <T> data carrier for this protocol.
 */

public interface UpperSocketPort<T extends ProtocolContainer> {
	/**
	 * Register a {@link LowerAdapter}
	 * @param adapter
	 */
	public void register(LowerSocketAdapter<T> adapter);
}
