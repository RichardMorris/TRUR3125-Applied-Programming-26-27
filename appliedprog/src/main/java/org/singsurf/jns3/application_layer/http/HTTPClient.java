package org.singsurf.jns3.application_layer.http;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.SocketFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.transport_layer.tcp.SocketMessage;
import org.singsurf.jns3.transport_layer.tcp.TCPSocket;

public class HTTPClient implements LowerSocketAdapter<SocketMessage>, TickListener {
	SocketFactory<SocketMessage> socketFactory;
	ProtocolDevice device;
	List<TCPSocket> sockets = new ArrayList<>();
	
	public HTTPClient(ProtocolDevice device) {
		this.device = device;
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void setSocketFactory(SocketFactory<SocketMessage> factory) {
		this.socketFactory = factory;
	}

	public void requestPage(IPAddress address, String path) {
		String request = getRequestString(path);
		
		var msg = new SocketMessage("http", device.getIPAddress(), 
				address, (short) 12345, (short) 80, request.getBytes());
		var socket =  socketFactory.createSocket("http://"+address);
		socket.bind(address, (short) 80);
		sockets.add((TCPSocket) socket);
		socket.send(msg);
	}

	public String getRequestString(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("GET ").append(path).append(" HTTP/0.1\r\n");
//		sb.append("Host: ").append(device.getIPAddress()).append("\r\n");
		sb.append("User-Agent: HTTPClient/0.1\r\n");
        sb.append("Accept: text/html\r\n");
		sb.append("Connection: close\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	@Override
	public void onTick() {
		for(var socket : sockets) {
			SocketMessage datagram;
			while((datagram = socket.poll()) != null) {
				String message = new String(datagram.getContents());
				device.getTerminal().display(message);
			}
		}
	}
}
