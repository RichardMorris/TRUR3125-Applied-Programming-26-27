package org.singsurf.jns3.generic_protocol;

import org.singsurf.jns3.data_link_layer.FrameReceiver;
import org.singsurf.jns3.data_link_layer.FrameSender;

/**
 * An adapter on the lower side of a protocols handling code.
 * @param <A> data carrier for the lower level protocol.
 */
public interface LowerPhysicalAdapter {

	public void setFrameSender(FrameSender sender);

	public FrameReceiver getFrameReceiver();
}
