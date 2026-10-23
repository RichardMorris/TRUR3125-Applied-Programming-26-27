package org.singsurf.jns3.generic_protocol;

import java.util.List;

public interface LowerAdapter<A extends ProtocolType> {

//	public void register(UpperPort<A> port);
	
	public List<ProtocolHandler<A>> getHandlers();
	
	public void setOutboundSender(ProtocolSender<A> s);

}
