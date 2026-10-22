package org.singsurf.jns3.generic_protocol;

import java.util.List;

public interface ProtocolProvider<A extends ProtocolType>  {
	
	public List<ProtocolHandler<A>> getHandlers();
}
