package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.network_layer.IPAddress;

public interface ProtocolSocket<T> {

	/**
	 * Sends a datagram through the socket.
	 * 
	 * @param datagram message to send
	 */
	void send(T datagram);

	/**
	 * Receives a datagram from the socket.
	 * @param datagram the datagram to receive
	 */
	void receive(T datagram);

	/**
	 * Binds the socket to a specific IP address and port number.
	 * @param address
	 * @param port
	 */
	void bind(IPAddress address, short port);

	/**
	 * Closes the socket and unbinds it from its controller.
	 */
	void close();

	String getName();

	short getPort();

	IPAddress getAddress();

}