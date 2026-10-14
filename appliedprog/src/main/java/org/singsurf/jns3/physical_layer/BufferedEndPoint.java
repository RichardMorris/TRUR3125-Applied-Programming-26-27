package org.singsurf.jns3.physical_layer;

import java.util.LinkedList;
import java.util.Queue;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;

public abstract class BufferedEndPoint extends PhysicalEndPoint implements TickListener {
	Queue<byte[]> buffer = new LinkedList<>();
	
	public BufferedEndPoint(String name) {
		super(name);
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.RELEASE, this);
	}

    protected void send(byte[] frame) {
        if (isConnected()) {
        	if (getConnection() instanceof BlockingConnection bc) {
        		if(bc.blocked(getReversed())) {
        			buffer.add(frame);
					return;
				}
        	}
        	
            getConnection().send(frame, getReversed());
        } else {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No connection set");
        }
    }   

    @Override
    public void onTick() {
    	if(!isConnected()) {
			return;
		}
    	
    	while(!buffer.isEmpty()) {
			if (getConnection() instanceof BlockingConnection bc) {
				if(bc.blocked(getReversed())) {
					return;
				}
			}
			byte[] frame = buffer.remove();
			getConnection().send(frame, getReversed());
		}
	}
}
