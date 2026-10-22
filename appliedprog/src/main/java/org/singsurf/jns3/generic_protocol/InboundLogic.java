package org.singsurf.jns3.generic_protocol;

/**
 * Interface for the in-bound logic once a type has been extracted.
 * @param <T> The type of the protocol that is being handled.
 * @param <L> The type of the lower level protocol that is being handled.
 */
public interface InboundLogic<T extends ProtocolType,L extends ProtocolType> {

	public void handle(T type, L lowerLevelType);

}
