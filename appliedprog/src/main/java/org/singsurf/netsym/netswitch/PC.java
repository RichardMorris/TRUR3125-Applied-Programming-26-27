package org.singsurf.netsym.netswitch;

public class PC extends IPDevice {

    public PC(IPAddr addr) {
        super(addr);
    }

    public void receive(Message msg) {
        if(addr.equals(msg.getAddress())) {
            System.out.println(addr.toString() + ": " + msg.getMessage());
        } else {
            System.out.println("Error: "+addr.toString() 
                + " != " +
                msg.getAddress().toString() + ": " + msg.getMessage());
        }
    }
}
