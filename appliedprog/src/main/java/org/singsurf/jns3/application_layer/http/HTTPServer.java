package org.singsurf.jns3.application_layer.http;

import java.io.InputStream;
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

/**
 * A simple HTTP server that listens for incoming HTTP requests and responds
 * with HTTP responses.
 */
public class HTTPServer implements LowerSocketAdapter<InternetMessage>, TickListener {
	public static final short PUBLIC_PORT = 80;
	SocketFactory<InternetMessage> socketFactory;
	ProtocolDevice device;
	List<TCPSocket> sockets = new ArrayList<>();
	String name;
	FileLoader fileLoader;
	
	public FileLoader getFileLoader() {
		return fileLoader;
	}

	public void setFileLoader(FileLoader fileLoader) {
		this.fileLoader = fileLoader;
	}

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

	public String getReplyHeader(int contentLength) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/0.1 200 OK\r\n");
		sb.append("Server: JNS-HTTP-Server/0.1\r\n");
		sb.append("Transfer-Encoding: chunked\r\n");
		sb.append("Content-Type: text/html; charset=ASCII\r\n");
		sb.append("Content-Length: ").append(contentLength).append("\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	public String get400Header() {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/0.1 400 Bad Request\r\n");
		sb.append("Server: JNS-HTTP-Server/0.1\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	public String get404Header() {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/0.1 404 Not Found\r\n");
		sb.append("Server: JNS-HTTP-Server/0.1\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	public String get405Header() {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/0.1 405 Method Not Allowed\r\n");
		sb.append("Server: JNS-HTTP-Server/0.1\r\n");
		sb.append("\r\n");
		return sb.toString();
	}

	public byte[] getFullResponse(String path) {
		var stream = loadFile(path);
		if(stream == null) {
			Jns3Logger.logError(name, "File not found: " + path);
			var header = get404Header();
			return header.getBytes();
		}
		try {
			var bytes = stream.readAllBytes();
			var header = getReplyHeader(bytes.length);
			byte[] res = new byte[header.length() + bytes.length];
			System.arraycopy(header.getBytes(), 0, res, 0, header.length());
			System.arraycopy(bytes, 0, res, header.length(), bytes.length);
			return res;
		} catch (Exception e) {
			Jns3Logger.logError(name, "Error reading file: " + path);
			return new byte[0];
		}
	}

	public InputStream loadFile(String path) {
		return fileLoader.loadFile(path);
	}

	public String lastRequest = null;
	@Override
	public void onTick() {
		for(var socket : sockets) {
			InternetMessage datagram;
			while((datagram = socket.poll()) != null) {
				Jns3Logger.logRec(name, datagram.toHeaderString());
				lastRequest = new String(datagram.getContents());
				var responseBytes = parseRequest(lastRequest);
				var response = new InternetMessage("http",
						datagram.getSrcIP(),	
						datagram.getDestIP(), 
						datagram.getSrcPort(), 
						datagram.getDestPort(), responseBytes);
				Jns3Logger.logSend(name, response.toMidString());
//				socket.send(response);
			}
		}
	}

	public byte[] parseRequest(String request) {
		var lines = request.split("\r\n");
		if(lines[0].startsWith("GET")) {
			var parts = lines[0].split(" ");
			if(parts.length >= 2) {
				return getFullResponse(parts[1]);
			}
			Jns3Logger.logError(name, "Malformed GET request: " + lines[0]);
			return get400Header().getBytes();
		} else {
			Jns3Logger.logError(name, "Unknown request type: "+lines[0]);
			return get405Header().getBytes();
		}
	}
}
