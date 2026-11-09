package org.singsurf.jns3.physical_layer;

import org.singsurf.jns3.Jns3Logger;

public class BlockingEndPoint extends PhysicalEndPoint implements BlockingByteSender {
	ByteReceiver byteReceiver;
	
	public BlockingEndPoint(String name) {
		super(name);
	}

	public boolean blocked() {
	    if (!isConnected()) {
	    	return true;
	    }
    	if (getConnection() instanceof BlockingConnection bc) {
			return bc.blocked(getReversed());
		}
    	return false;
	}
	
	@Override
	public void send(byte[] frame) {
		if(blocked()) {
            Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "Sending frame while blocked");
		} else {
			getConnection().send(frame, getReversed());
	    }
	}

	@Override
	public void receive(byte[] frame) {
		if(byteReceiver != null) {
			byteReceiver.receive(frame);
		} else {
			Jns3Logger.log(getName(), Jns3Logger.LogLevel.ERROR, "No byte receiver set");
		}		
	}

	void setByteReceiver(ByteReceiver byteReceiver) {
		this.byteReceiver = byteReceiver;
	}
	

}
