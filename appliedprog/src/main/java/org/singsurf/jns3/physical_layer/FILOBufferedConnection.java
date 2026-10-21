package org.singsurf.jns3.physical_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.hardware.GlobalWallClock;

/**
 * Like a {@link BufferedConnection} but the frames are sent in reverse order (FILO).
 * Ensures a scrambling of frame order.
 */
public class FILOBufferedConnection extends PhysicalConnection implements GlobalWallClock.TickListener {
	List<BufferedFrame> inbuffer = new ArrayList<>();
	List<BufferedFrame> outbuffer;
	
	record BufferedFrame(byte[] frame, boolean reversed) {	}
	
	public FILOBufferedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
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
			BufferedFrame bufferedFrame = outbuffer.removeLast();
			receive(bufferedFrame.frame(), bufferedFrame.reversed());
		}
	}

}
