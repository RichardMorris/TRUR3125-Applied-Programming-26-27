package org.singsurf.jns3.data_link_layer;

import java.util.LinkedList;
import java.util.Queue;

import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.physical_layer.BlockingByteSender;

public class BufferedFrameSender implements FrameSender, TickListener {
	Queue<EthernetFrame> buffer = new LinkedList<>();
	BlockingByteSender byteSender;
	
	public BufferedFrameSender() {
		GlobalWallClock.getInstance().addTickListener(GlobalWallClock.RELEASE, this);
	}

	@Override
	public void send(EthernetFrame frame) {
		if(byteSender.blocked()) {
			buffer.add(frame);
		} else {
			byteSender.send(frame.getBytes());
		}
	}

	@Override
	public void onTick() {
    	
    	while(!buffer.isEmpty()) {
    		if(!byteSender.blocked()) {
    			send(buffer.remove());
    		} else {
				return;
			}
		}
	}

	void setByteSender(BlockingByteSender byteSender) {
		this.byteSender = byteSender;
	}

}
