package org.singsurf.jns3.network_layer;

public class SubnetMask extends IPAddress {

	public SubnetMask(String addr) {
		super(addr);
	}

	public SubnetMask(IPAddress maskIP) {
		super(maskIP.getBytes());
	}

	public boolean matches(IPAddress src, IPAddress dst) {
		var srcBytes = src.getBytes();
		var dstBytes = dst.getBytes();
		var maskBytes = this.getBytes();
		for(int i=0; i<4; i++) {
			if((srcBytes[i] & maskBytes[i]) != (dstBytes[i] & maskBytes[i])) {
				return false;
			}
		}
		return true;
	}
}
