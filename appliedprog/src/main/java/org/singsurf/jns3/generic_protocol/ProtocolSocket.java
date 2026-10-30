package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.network_layer.IPAddress;

public abstract class ProtocolSocket<T> {

	protected String name;
	protected short port;
	protected IPAddress boundAddress;

	public static boolean LOG_MESSAGES = false;



	public ProtocolSocket(String name) {
		super();
		this.name = name;
	}


	public abstract void send(T datagram);

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
