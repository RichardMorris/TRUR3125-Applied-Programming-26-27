package org.singsurf.jns3.application_layer.http;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.SocketFactory;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.ProtocolDevice;
import org.singsurf.jns3.transport_layer.tcp.InternetMessage;
import org.singsurf.jns3.transport_layer.tcp.TCPSocket;

public class HTTPServer implements LowerSocketAdapter<InternetMessage>, TickListener {
	public static final short PUBLIC_PORT = 80;
	SocketFactory<InternetMessage> socketFactory;
	ProtocolDevice device;
	List<TCPSocket> sockets = new ArrayList<>();
	String name;
	public HTTPServer(ProtocolDevice device) {
		this.device = device;
		name = device.getName() + "-HTTPD";
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void setSocketFactory(SocketFactory<InternetMessage> factory) {
		this.socketFactory = factory;
		var socket = socketFactory.createSocket(device.getName() + "-HTTPD");
		var multicastAddress = device.getMulticastAddress();
		socket.bind(multicastAddress, PUBLIC_PORT);
		sockets.add((TCPSocket) socket);
	}

	public String getReplyHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/0.1 200 OK\r\n");
		sb.append("Server: JNS-HTTP-Server/0.1\r\n");
		sb.append("Transfer-Encoding: chunked\r\n");
		sb.append("Content-Type: text/html; charset=ASCII\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	public String lastRequest = null;
	@Override
	public void onTick() {
		for(var socket : sockets) {
			InternetMessage datagram;
			while((datagram = socket.poll()) != null) {
				Jns3Logger.logRec(name, datagram.toHeaderString());
				lastRequest = new String(datagram.getContents());
			}
		}
	}
}
