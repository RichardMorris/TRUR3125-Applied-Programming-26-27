package org.singsurf.jns3.generic_protocol;

public interface ProtocolSender<T extends ProtocolType> {

	public void send(T protocolType);

}
