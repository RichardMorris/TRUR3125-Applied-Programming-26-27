package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;

public class ThrottledConnection extends PhysicalConnection implements BlockingConnection, TickListener {
	public static final int CAT3 = 16; // 16 MHz
	public static final int CAT4 = 20; // 20 MHz
	public static final int CAT5 = 100; // 100 MHz
	public static final int CAT6 = 250; // 250 MHz
	public static final int CAT7 = 600; // 600 MHz
	public static final int CAT8 = 2000; // 2 GHz
	
	final int bandwidth;
	int count=0;
	
	public ThrottledConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, int bandwidth) {
		super(dev1, dev2);
		this.bandwidth = bandwidth;
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT, this);
	}

	@Override
	public boolean blocked(boolean reversed) {
		return count >= bandwidth;
	}

	@Override
	protected void send(byte[] frame, boolean reversed) {
		if(blocked(reversed)) {
			Jns3Logger.logError(
				reversed ? dev2.getName() : dev1.getName(), 
				"Connection is blocked, frame dropped");
			return;
		}
		++count;
		receive(frame, reversed);
		
	}

	@Override
	public void onTick() {
		count = 0;
	}

}
