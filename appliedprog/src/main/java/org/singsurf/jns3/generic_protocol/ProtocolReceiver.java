package org.singsurf.jns3.generic_protocol;

public interface ProtocolReceiver<T extends ProtocolType> {

	public void receive(T protocolType);

}
