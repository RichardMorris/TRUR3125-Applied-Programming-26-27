package org.singsurf.jns3.generic_protocol;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicInterface<A extends ProtocolType, B extends ProtocolType, C extends ProtocolType>
	implements ProtocolRegestrator<B>, ProtocolProvider<A> {

	List<ProtocolHandler<A>> handlers = new ArrayList<>();
	
	ProtocolDelegator<B> delegator = new ProtocolDelegator<>("BasicInterface");

	@Override
	public void register(ProtocolProvider<B> higher) {
		for(ProtocolHandler<B> h: higher.getHandlers()) {
			delegator.addHandler(h);
		}
	}

	public List<ProtocolHandler<A>> getHandlers() {
		return handlers;
	}

}
