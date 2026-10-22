package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.Jns3Logger;

public class NullProtocolType implements ProtocolType {
	String name = "NullProtocolType";

	@Override
	public byte[] getBytes() {
		Jns3Logger.logError(name, "get Bytes() called on NullProtocolType");
		return new byte[0];
	}

	@Override
	public byte[] getContents() {
		Jns3Logger.logError(name, "get Contents() called on NullProtocolType");
		return new byte[0];
	}

}
