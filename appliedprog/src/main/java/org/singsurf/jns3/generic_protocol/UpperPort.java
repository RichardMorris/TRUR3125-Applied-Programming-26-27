package org.singsurf.jns3.generic_protocol;

/**
 * A port on the upper side of a protocols allowing higher level
 * protocols to register their {@link ProtocolHandler} classes and query the 
 * {@link ProtocolSender} and callback {@link ProtocolReceiver}
 * @param <T> data carrier for this protocol.
 */

public interface UpperPort<T extends ProtocolType> {
	/**
	 * Register a {@link LowerAdapter}
	 * @param adapter
	 */
	public void register(LowerAdapter<T> adapter);

}
