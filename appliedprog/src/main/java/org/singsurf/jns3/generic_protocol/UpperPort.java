package org.singsurf.jns3.generic_protocol;

public interface UpperPort<B extends ProtocolType> {
		
	public void register(LowerAdapter<B> port);

}
