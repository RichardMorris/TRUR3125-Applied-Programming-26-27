package org.singsurf.jns3.generic_protocol;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Handled;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Ignored;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Invalid;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Modifies;

public class ProtocolDelegator<T extends ProtocolType> implements ProtocolReceiver<T> {
	String name;
	List<ProtocolHandler<T>> handlers = new ArrayList<>();
	
	
	public ProtocolDelegator(String name) {
		this.name = name;
	}
	
	@Override
	public void receive(T message) {
		boolean modified = true;
		
		while(modified) {
			modified = false;
			for (var handler : handlers) {
				
				var state = handler.handle(message);
				
				switch(state) {
					case Handled<T> h -> { return; }
					case Ignored<T> i -> { break; }
					case Invalid<T> i -> {
						Jns3Logger.logError(name, "Invalid message: " + i.reason);
						return;
					}
					case Modifies<T> m -> {
						message = m.getModifiedMessage();
						modified = true;
						break;
					}
					default ->  {
						Jns3Logger.logError(name, "Unkown return state: " + state.getClass().getName());
						return;
					}
				}
			}			
		}
		Jns3Logger.logError(name, "No handler found for message: " + message);		
	}
	
	public void addHandler(ProtocolHandler<T> handler) {
		handlers.add(handler);
	}
	
	public void removeHandler(ProtocolHandler<T> handler) {
		handlers.remove(handler);
	}
}
