package org.singsurf.jns3.ipswitch;

public class PC extends IPDevice {

    public PC(IPAddr addr) {
        super(addr);
    }

    public void receive(IPMessage msg) {
        if(addr.equals(msg.getAddress())) {
            System.out.println(addr.toString() + ": " + msg.getMessage());
        } else {
            System.out.println("Error: "+addr.toString() 
                + " != " +
                msg.getAddress().toString() + ": " + msg.getMessage());
        }
    }
}
