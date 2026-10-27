package org.singsurf.jns3.physical_layer;

/**
 * Used by connections and other devices that can be blocked.
 * <p>
 * See 
 */
public interface BlockingConnection {
	boolean blocked(boolean reversed);
}
