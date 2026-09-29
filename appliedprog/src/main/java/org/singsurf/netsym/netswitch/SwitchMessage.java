package org.singsurf.netsym.netswitch;

import org.singsurf.netsym.Message;
import org.singsurf.netsym.IPAddr;

/**
 * Represents a message going to a particular IP address
 * SwitchMessage
 */
public class SwitchMessage implements Message {
    IPAddr ipaddr;
    String message;

    /**
     * Build a message to go to a particular IP addr
     * @param ipaddr
     * @param message
     */
    public SwitchMessage(IPAddr ipaddr, String message) {
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
    public void setIpaddr(IPAddr ipaddr2) {
        this.ipaddr = ipaddr2;
    }

    public IPAddr getIpaddr() {
        return ipaddr;
    }

}
