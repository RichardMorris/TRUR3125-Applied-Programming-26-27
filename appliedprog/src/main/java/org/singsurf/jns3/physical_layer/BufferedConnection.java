package org.singsurf.jns3.physical_layer;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.hardware.GlobalWallClock;

public class BufferedConnection extends PhysicalConnection implements GlobalWallClock.TickListener {
	List<BufferedFrame> buffer = new ArrayList<>();
	
	record BufferedFrame(byte[] frame, boolean reversed) {	}
	
	public BufferedConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
		super(dev1, dev2);
		
		GlobalWallClock.getInstance().addTickListener(this);
	}

	@Override
	protected void send(byte[] frame, boolean reversed) {
		buffer.add(new BufferedFrame(frame, reversed));
	}

	@Override
	public void onTick() {
		
		while (!buffer.isEmpty()) {
			BufferedFrame bufferedFrame = buffer.remove(0);
			receive(bufferedFrame.frame(), bufferedFrame.reversed());
		}
	}

}
