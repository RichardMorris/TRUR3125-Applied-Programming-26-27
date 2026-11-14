package org.singsurf.jns3.physical_layer;

/**
 * A ByteSender that can be blocked.
 */
public interface BlockingByteSender extends ByteSender {
	
	public boolean blocked();
}
