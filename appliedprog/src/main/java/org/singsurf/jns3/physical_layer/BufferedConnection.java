package org.singsurf.jns3.physical_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.hardware.GlobalWallClock;

/**
 * A connection that buffers all it content
 * until the next tick.
 */
public class BufferedConnection extends PhysicalConnection implements GlobalWallClock.TickListener {
	List<BufferedFrame> inbuffer = new ArrayList<>();
	List<BufferedFrame> outbuffer;
	
	record BufferedFrame(byte[] frame, boolean reversed) {	}
	
	public BufferedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
		super(dev1, dev2);
		
		GlobalWallClock.getInstance().addTickListener(this);
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT,
			() -> {
				outbuffer = inbuffer;
				inbuffer = new ArrayList<>();
				
			});
	}

	@Override
	protected void send(byte[] frame, boolean reversed) {
		inbuffer.add(new BufferedFrame(frame, reversed));
	}

	@Override
	public void onTick() {
		while (!outbuffer.isEmpty()) {
			BufferedFrame bufferedFrame = outbuffer.remove(0);
			receive(bufferedFrame.frame(), bufferedFrame.reversed());
		}
	}

}
