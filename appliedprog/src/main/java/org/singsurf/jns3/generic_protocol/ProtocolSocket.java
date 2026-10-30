package org.singsurf.jns3.generic_protocol;

import java.util.ArrayDeque;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.udp.UDPController;

/**
 * A socket that can be used by applications to send 
 * and receive UDP/TCP datagrams.
 * <p>
 * The socket maintains an internal queue of received datagrams,
 * which can be retrieved using the {@link #poll()} method.
 * <p>
 * Sockets can be created by applications using a {@link SocketFactory}.
 * <p>
 * Sockets are identified by ip address and port number,
 * which are be set using the {@link #bind(IPAddress, short)} method.
 * @param <T>
 */
public abstract class ProtocolSocket<T> {

	protected String name;
	protected short port;
	protected IPAddress boundAddress;
	protected Queue<T> queue = new ArrayDeque<>();

	public static boolean LOG_MESSAGES = false;



	protected ProtocolSocket(String name) {
		super();
		this.name = name;
	}

	/**
	 * Sends a datagram through the socket.
	 * 
	 * @param datagram message to send
	 */
	public abstract void send(T datagram);

	/**
	 * Receives a datagram from the socket.
	 * Datagrams are queued in the socket's internal queue, 
	 * and can be retrieved using the {@link #poll()} method.
	 * @param datagram the datagram to receive
	 */
	public void receiveDatagram(T datagram) {
		var len = Math.min(datagram.toString().length(), 200);
		if(LOG_MESSAGES)
			Jns3Logger.logInfo(name, "Received datagram " + datagram.toString().substring(0, len));
		queue.add(datagram);
	}

	/**
	 * Polls the socket for an in bound datagram. If no datagram is available, returns null.
	 * @return the next datagram in the queue, or null if no datagram is available.
	 */
	public T poll() {
		return queue.poll();
	}


	/**
	 * Closes the socket and unbinds it from the {@link UDPController}.
	 */
	public abstract void close();

	/**
	 * Binds the socket to a specific IP address and port number.
	 * @param address
	 * @param boundPort
	 */
	public abstract void bind(IPAddress address, short boundPort);

	public String getName() {
		return name;
	}

	public short getPort() {
		return port;
	}

	public IPAddress getAddress() {
		return boundAddress;
	}


}
