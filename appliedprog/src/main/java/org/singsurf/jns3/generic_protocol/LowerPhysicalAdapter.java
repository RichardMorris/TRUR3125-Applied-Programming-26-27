package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.physical_layer.BlockingByteSender;
import org.singsurf.jns3.physical_layer.ByteReceiver;

/**
 * An adapter on the lower side of a protocols handling code.
 * @param <A> data carrier for the lower level protocol.
 */
public interface LowerPhysicalAdapter {

	public void setByteSender(BlockingByteSender sender);

	public ByteReceiver getByteReceiver();
}
