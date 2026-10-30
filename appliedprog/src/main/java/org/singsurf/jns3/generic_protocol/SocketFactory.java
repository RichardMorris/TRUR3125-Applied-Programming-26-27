package org.singsurf.jns3.generic_protocol;

public interface SocketFactory<T> {
	
	ProtocolSocket<T> createSocket(String name);

}
