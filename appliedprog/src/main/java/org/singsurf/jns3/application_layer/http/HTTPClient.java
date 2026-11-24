package org.singsurf.jns3.application_layer.http;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
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
	String name;
	List<TCPSocket> sockets = new ArrayList<>();
	String compiledMessage = "";
	List<String> receivedPages = new ArrayList<>();
	
	
	public HTTPClient(ProtocolDevice device) {
		this.device = device;
		this.name = device.getName() + "-HTC";
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	public void setSocketFactory(SocketFactory<SocketMessage> factory) {
		this.socketFactory = factory;
	}

	public void requestPage(IPAddress address, String path) {
		String request = getRequestString(path);
		
		var msg = new SocketMessage("http", device.getIPAddress(), 
				(short) 12345, address, (short) 80, request.getBytes());
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
				parseResponse(message);
			}
		}
	}

	void parseResponse(String message) {
		compiledMessage += message;
		if(compiledMessage.contains("\r\n\r\n")) {
			String[] parts = compiledMessage.split("\r\n\r\n", 2);
			if(parts.length < 2) {
				return;
			}
			String header = parts[0];
			String body = parts[1];
			var headerLines = header.split("\r\n");
			if(headerLines.length > 0) {
				String statusLine = headerLines[0];
				if(statusLine.startsWith("HTTP/")) {
					String[] statusParts = statusLine.split(" ", 3);
					if(statusParts.length >= 2) {
						String statusCode = statusParts[1];
						if(statusCode.equals("200")) {
						} else {
							Jns3Logger.logError(name, "Received non-200 status code: " + statusCode);
							return;
						}
					}
				}
				int contentLength = -1;
				for(String line : headerLines) {
					if(line.toLowerCase().startsWith("content-length:")) {
						String[] parts2 = line.split(":", 2);
						if(parts2.length == 2) {
							String lengthStr = parts2[1].trim();
							try {
								contentLength = Integer.parseInt(lengthStr);
							} catch(NumberFormatException e) {
								Jns3Logger.logError(name, "Invalid Content-Length value: " + lengthStr);
							}
						}
					}
				}
				if(contentLength >= 0) {
					if(body.length() >= contentLength) {
						String responseBody = body.substring(0, contentLength);
						getReceivedPages().add(responseBody);
						Jns3Logger.logRec(name, "Received response body: " + responseBody);
						compiledMessage = "";
					} else {
						Jns3Logger.logInfo(name, "Waiting for more data. Current body length: " + body.length() + ", expected: " + contentLength);
					}
				} else {
					Jns3Logger.logInfo(name, "Content-Length header not found.");
				}
				
			}
		}		
	}

	public List<String> getReceivedPages() {
		return receivedPages;
	}
}
