package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;

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
	
	/**
	 * Accepts a visitor for the lower level protocol, and attempts 
	 * to convert the container to the protocols data type.
	 * If the data can be extracted the one of the visitors visit methods
	 * should be called, for the IPProtocol would call
	 * {@link ProtocolVisitor#visit(IPProtocol, IPPacket)}.
	 * via the code {@code visitor.visit(this, packet)}.
	 * If the data cannot be extracted the method should return {@code null}.
	 * @param <R> the return type of the visitor.
	 * @param visitor the visitor to accept.
	 * @param container the data carrier for the lower level protocol.
	 * @return the return value of the visitor's visit method or {@code null} if
	 * the container could not be converted to the protocols data type.
	 * @since week 18
	 */
	public default <R> R accept(ProtocolVisitor<R> visitor, A container) {
		return null;
	}

}
