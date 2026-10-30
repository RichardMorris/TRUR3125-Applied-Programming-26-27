package org.singsurf.jns3.generic_protocol;

import java.util.ArrayDeque;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.network_layer.IPAddress;

public abstract class ProtocolSocket<T> {

	protected String name;
	protected short port;
	protected IPAddress boundAddress;
	public Queue<T> queue = new ArrayDeque<>();

	public static boolean LOG_MESSAGES = false;



	public ProtocolSocket(String name) {
		super();
		this.name = name;
	}


	public T poll() {
		return queue.poll();
	}

	public abstract void send(T datagram);

	public void receiveDatagram(T datagram) {
		var len = Math.min(datagram.toString().length(), 200);
		if(LOG_MESSAGES)
			Jns3Logger.logInfo(name, "Received datagram " + datagram.toString().substring(0, len));
		queue.add(datagram);
	}

	
	public String getName() {
		return name;
	}

	public Object getAddress() {
		return getBoundAddress();
	}

	public short getPort() {
		return port;
	}

	public IPAddress getBoundAddress() {
		return boundAddress;
	}

	public void setBoundAddress(IPAddress boundAddress) {
		this.boundAddress = boundAddress;
	}






}
