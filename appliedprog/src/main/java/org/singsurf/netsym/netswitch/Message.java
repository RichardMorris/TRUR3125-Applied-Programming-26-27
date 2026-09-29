package org.singsurf.netsym.netswitch;

/**
 * Represents a message going to a particular IP address
 * SwitchMessage
 */
public class Message {
    IPAddr ipaddr;
    String message;

    /**
     * Build a message to go to a particular IP addr
     * @param ipaddr
     * @param message
     */
    public Message(IPAddr ipaddr, String message) {
        this.ipaddr = ipaddr;
        this.message = message;
    }

    /**
     * We can read, but not change the message
     * @return the message
     */
    public String getMessage() {
        return message;
    }


    /**
     * It may be necessary to change the address at some point in the future.
     * @param ipaddr
     */
    public void setAddress(IPAddr ipaddr2) {
        this.ipaddr = ipaddr2;
    }

    /**
     * Get the IP address
     * @return the IP address
     */
    public IPAddr getAddress() {
        return ipaddr;
    }

}
