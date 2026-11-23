package org.singsurf.jns3.transport_layer.tcp;

import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPAddress;

public class InternetMessage implements ProtocolContainer {
	String protocolName;
	IPAddress srcIP;
	IPAddress destIP;
	short srcPort;
	short destPort;
	byte[] contents;

	public InternetMessage(String protocolName, IPAddress srcIP, IPAddress destIP, short srcPort, short destPort,
			byte[] contents) {
		super();
		this.protocolName = protocolName;
		this.srcIP = srcIP;
		this.destIP = destIP;
		this.srcPort = srcPort;
		this.destPort = destPort;
		this.contents = contents;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getContents() {
		return contents;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String toHeaderString() {
		return protocolName + "::" + srcIP + ":" + srcPort + "->"+ destIP + ":" + destPort;
	}

	@Override
	public String toString() {
		var str = new String(contents);
		if(str.length()>20) {
			str = str.substring(0, 20);
		}
		return protocolName + "::" + srcIP + ":" + srcPort + "->"+ destIP + ":" + destPort + " "+ str;
	}

	public String toMidString() {
		var str = new String(contents);
		var sb = new StringBuilder();
		sb.append(toHeaderString());
		var lines = str.split("\n");
		if(lines.length<10) {
			sb.append(str);
		} else {
			for(int i=0;i<9;++i) {
				sb.append(lines[i]).append("\n");
			}
			sb.append("...\n");
			sb.append(lines[lines.length-2]).append("\n");
			sb.append(lines[lines.length-1]).append("\n");
		}
		return sb.toString();
	}

	public String getProtocolName() {
		return protocolName;
	}

	public IPAddress getSrcIP() {
		return srcIP;
	}

	public IPAddress getDestIP() {
		return destIP;
	}

	public short getSrcPort() {
		return srcPort;
	}

	public short getDestPort() {
		return destPort;
	}

	
}
