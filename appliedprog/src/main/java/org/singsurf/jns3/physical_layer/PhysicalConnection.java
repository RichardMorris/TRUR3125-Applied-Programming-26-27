package org.singsurf.jns3.physical_layer;

/**
 * Represents a physical ethernet cable connection between two devices.
 * Frames sent by one device are received by the other.
 */
public abstract class PhysicalConnection {
    PhysicalEndPoint dev1;
    PhysicalEndPoint dev2;

    public PhysicalConnection(PhysicalEndPoint dev1, PhysicalEndPoint dev2) {
        this.dev1 = dev1;
        this.dev2 = dev2;
        dev1.setConnection(this,false);
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

}
