package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;

/**
 * Models a half-duplex connection.
 * Only allows traffic through in one dirction per tick.
 */
public class HalfDuplexConnection extends PhysicalConnection implements BlockingConnection, TickListener {
	/** State of the connection. */
	enum State {
		IDLE, FORWARD, REVERSE
	}
	State state = State.IDLE;
	
	public HalfDuplexConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2, int bandwidth) {
		super(dev1, dev2);
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.INIT, this);
	}

	@Override
	public boolean blocked(boolean reversed) {
		if(state == State.IDLE) {
			return false;
		}
		if(reversed && state == State.FORWARD) {
			return true;
		}
		if(!reversed && state == State.REVERSE) {
			return true;
		}
		return false;
	}

	@Override
	protected void send(byte[] frame, boolean reversed) {
		if(blocked(reversed)) {
			Jns3Logger.logError(
				reversed ? dev2.getName() : dev1.getName(), 
				"Connection is blocked, frame dropped");
			return;
		}
		state = reversed ? State.REVERSE : State.FORWARD;
		receive(frame, reversed);
		
	}

	@Override
	public void onTick() {
		state = State.IDLE;
	}

}
