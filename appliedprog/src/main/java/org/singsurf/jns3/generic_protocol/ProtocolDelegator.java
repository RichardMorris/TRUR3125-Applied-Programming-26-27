package org.singsurf.jns3.generic_protocol;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Handled;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Ignored;
import org.singsurf.jns3.generic_protocol.ProtocolHandler.Invalid;

/**
 * Delegates processing of a message to handlers
 * provided either by this protocol, or those provided
 * by a higher level protocol.
 * 
 * @param <T> container type for this protocol
 */
public class ProtocolDelegator<T extends ProtocolType> implements ProtocolReceiver<T> {
	String name;
	List<ProtocolHandler<T>> handlers = new ArrayList<>();
	
	public ProtocolDelegator(String name) {
		this.name = name;
	}
	
	@Override
	public void receive(T message) {
		for (var handler : handlers) {			
			var state = handler.handle(message);
			switch(state) {
			case Handled h -> { return; }
			case Ignored i -> { break; }
			case Invalid i -> {
				Jns3Logger.logError(name, "Invalid message: " + i.reason());
				return;
				}
			}
		}
		Jns3Logger.logError(name, "No handler found for message: " + message);		
	}
	
	public void addHandler(ProtocolHandler<T> handler) {
		var b = handlers.add(handler);
		if(!b) {
			Jns3Logger.logError(name, "Failed to add handler: " + handler);
		}
	}
	
	public void removeHandler(ProtocolHandler<T> handler) {
		var b = handlers.remove(handler);
		if(!b) {
			Jns3Logger.logError(name, "Failed to remove handler: " + handler);
		}
	}
}
