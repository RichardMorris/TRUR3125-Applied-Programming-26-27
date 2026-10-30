package org.singsurf.jns3.generic_protocol;

/**
 * An adapter on the lower side of a protocols handling code.
 * @param <A> data carrier for the lower level protocol.
 */
public interface LowerSocketAdapter<A extends ProtocolContainer> {


	/**
	 * Sets the factory used to create sockets
	 * @param factory the factory to use to create sockets
	 */
	public void setSocketFactory(SocketFactory<A> factory);
}
