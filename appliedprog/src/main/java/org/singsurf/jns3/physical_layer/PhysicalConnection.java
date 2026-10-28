package org.singsurf.jns3.physical_layer;

/**
 * Base class for all physical connections.
 * To configure two end-point devices must be supplied,
 * messages send via one end-points to the 
 * {@code send(byte[] frame, boolean reversed)} 
 * receive method of subclasses are normally 
 * delivered to the other end-point using the
 * {@code receive(byte[] frame, boolean reversed)} method.
 * <p>
 * Two connections can be joined end to end 
 * by using the {@code getVirtualEndPoint(int index)} method.
 */
public abstract class PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;
    
    class VirtualEndPoint extends PhysicalEndPoint {
    	int end;
    	
		public VirtualEndPoint(int end) {
			super("VirtualEndPoint");
			this.end = end;
		}

		@Override
		protected void receive(byte[] frame) {
			if (end == 0) {
				PhysicalConnection.this.send(frame, false);
			} else {
				PhysicalConnection.this.send(frame, true);
			}
		}
	}

    public PhysicalConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        if(dev1!=null) 
        	dev1.setConnection(this,false);
        if(dev2!=null) 
            dev2.setConnection(this,true);
    }

    /**
     * Sends an as set of bytes from the sender device 
     * to the other device.
     * Implementing classes should call 
     * {@code receive(frame, reversed)} with the reversed parameter unchanged.
     * @param frame the data be sent
     * @param reversed whether the sender is the reversed end of the connection
     */
    protected abstract void send(byte[] frame, boolean reversed);

    /**
     * Receives an as set of bytes and delivers it to the other device
     * using the reversed flag to determine the sender.
     * @param frame the data being received
     * @param reversed whether the sender is the reversed end of the connection
     */
    protected void receive(byte[] frame, boolean reversed) {
        if(reversed) {
            dev1.receive(frame);
        } else {
            dev2.receive(frame);
        }
    }

    public PhysicalEndPoint getVirtualEndPoint(int index) {
		return new VirtualEndPoint(index);
	}
    
    public void setEnpoint1(PhysicalEndPoint dev) {
		this.dev1 = dev;
		dev1.setConnection(this,false);
	}
    public void setEnpoint2(PhysicalEndPoint dev) {
		this.dev2 = dev;
		dev2.setConnection(this,true);
	}

}
