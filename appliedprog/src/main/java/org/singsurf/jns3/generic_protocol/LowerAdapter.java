package org.singsurf.jns3.generic_protocol;

import java.util.List;

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

	/**
	 * Sets the callback function provide by the lower level
	 * protocol for handling modified messages.
	 * @param receiver the receiver for new or modified messages.
	 */
	public void setInboundCallback(ProtocolReceiver<A> receiver);
	
	/**
	 * Sets the sender used to for transmitting out bound messages.
	 * @param sender
	 */
	public void setOutboundSender(ProtocolSender<A> sender);
}
