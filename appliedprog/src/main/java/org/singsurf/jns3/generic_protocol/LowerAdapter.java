package org.singsurf.jns3.generic_protocol;

import java.util.List;

import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPProtocol;

/**
 * An adapter on the lower side of a protocols handling code.
 * @param <A> data carrier for the lower level protocol.
 */
public interface LowerAdapter<A extends ProtocolContainer> {

	/**
	 * Provides all the handlers used by the lower level's 
	 * {@link ProtocolDelegator}
	 * @return list of handlers in the order they should be processed.
	 */
	public List<ProtocolHandler<A>> getInboundHandlers();

//	/**
//	 * Sets the callback function provide by the lower level
//	 * protocol for handling modified messages.
//	 * @param receiver the receiver for new or modified messages.
//	 */
//	public void setInboundCallback(ProtocolReceiver<A> receiver);
	
	/**
	 * Sets the sender used to for transmitting out bound messages.
	 * @param sender
	 */
	public void setOutboundSender(ProtocolSender<A> sender);
	
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
	};
}
