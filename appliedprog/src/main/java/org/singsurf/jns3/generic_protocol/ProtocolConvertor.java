package org.singsurf.jns3.generic_protocol;

public interface ProtocolConvertor<L extends ProtocolType, H extends ProtocolType> {

	public H extract(L type);

	public L encapsulate(H type);
}
