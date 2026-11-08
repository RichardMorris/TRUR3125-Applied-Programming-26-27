package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.MultiPortReceiver;

/**
 * An adapter on the lower side of a protocols handling code.
 * @param <A> data carrier for the lower level protocol.
 */
public interface LowerPhysicalAdapter {

	public void setByteSenders(BlockingEndPoint[] bep);

	public MultiPortReceiver getMultiByteReceiver();
}
