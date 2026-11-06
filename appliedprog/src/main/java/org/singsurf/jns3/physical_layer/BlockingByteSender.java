package org.singsurf.jns3.physical_layer;

public interface BlockingByteSender extends ByteSender {
	
	public boolean blocked();
}
