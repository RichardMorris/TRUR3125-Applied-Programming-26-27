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
public abstract class PolledSocket<T extends ProtocolContainer> implements ProtocolSocket<T> {
	public static boolean LOG_MESSAGES = false;

	protected final String name;
	protected final Queue<T> queue = new ArrayDeque<>();
	protected final ProtocolReceiver<T> receiver;

	protected short port;
	protected IPAddress address;

	/**
	 * Creates a new ProtocolSocket with the given name.
	 * @param name used for logging purposes.
	 */

	protected PolledSocket(String name) {
		this.name = name;
		this.receiver = null;
	}

	public PolledSocket(String name, ProtocolReceiver<T> receiver) {
		this.name = name;
		this.receiver = receiver;
	}

	/**
	 * Sends a datagram through the socket.
	 * 
	 * @param datagram message to send
	 */
	@Override
	public abstract void send(T datagram);

	/**
	 * Receives a datagram from the socket.
	 * If the socket has a receiver, the datagram is passed to the receiver's receive method,
	 * otherwise the datagram is added to the socket's internal queue
	 * and can be retrieved using the {@link #poll()} method.
	 * @param datagram the datagram to receive
	 */
	@Override
	public void receive(T datagram) {
		var len = Math.min(datagram.toString().length(), 200);
		if(LOG_MESSAGES)
			Jns3Logger.logInfo(name, "Received datagram " + datagram.toString().substring(0, len));
		if(receiver != null) {
			receiver.receive(datagram);
		} else {
			queue.add(datagram);
		}
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
	@Override
	public abstract void close();

	/**
	 * Binds the socket to a specific IP address and port number.
	 * @param address
	 * @param boundPort
	 */
	@Override
	public abstract void bind(IPAddress address, short boundPort);

	@Override
	public String getName() {
		return name;
	}

	@Override
	public short getPort() {
		return port;
	}

	@Override
	public IPAddress getAddress() {
		return address;
	}


}
