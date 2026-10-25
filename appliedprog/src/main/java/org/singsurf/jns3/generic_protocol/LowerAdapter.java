package org.singsurf.jns3.generic_protocol;

import java.util.List;

public interface LowerAdapter<A extends ProtocolType> {

	public List<ProtocolHandler<A>> getInboundHandlers();

	public void setInboundCallback(ProtocolReceiver<A> r);
	
	public void setOutboundSender(ProtocolSender<A> s);
}
