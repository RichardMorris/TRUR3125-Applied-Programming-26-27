package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.physical_layer.BlockingEndPoint;
import org.singsurf.jns3.physical_layer.MultiPortByteReceiver;

/**
 * An adapter on the lower side of a protocols handling byte arrays.
 */
public interface LowerPhysicalAdapter {

	public void setByteSenders(BlockingEndPoint[] bep);

	public MultiPortByteReceiver getMultiByteReceiver();
}
