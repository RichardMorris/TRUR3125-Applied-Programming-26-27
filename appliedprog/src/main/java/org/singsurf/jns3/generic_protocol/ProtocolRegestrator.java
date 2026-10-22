package org.singsurf.jns3.generic_protocol;

public interface ProtocolRegestrator<B extends ProtocolType>  {

	/**
	 * Causes the handlers from Register a higher level protocol
	 * to be added.
	 * 
	 * @param higher
	 */
	public void register(ProtocolProvider<B> higher);	
}
