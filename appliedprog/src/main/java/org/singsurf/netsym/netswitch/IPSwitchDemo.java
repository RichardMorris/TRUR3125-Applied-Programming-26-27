package org.singsurf.netsym.netswitch;

import java.util.ArrayList;
import java.util.List;

public class IPSwitchDemo {
    IPSwitch root;
    List<IPAddr> addressed = new ArrayList<>();

    public IPSwitchDemo() {
        root = new IPSwitch(new IPAddr("138.253.0.0"), 16);
        addressed.add(root.getAddr());
        System.out.println("Root switch initialized with address: " + root.getAddr());
        for (int i = 0; i < 10; i++) {
            int subnet = (int)(Math.random()*255)+1;
            IPAddr subnetAddr = new IPAddr("138.253."+subnet+".0");
            IPSwitch sw = new IPSwitch(subnetAddr, 24);
            root.addDevice(sw);
            addressed.add(subnetAddr);
            System.out.println("Added subnet switch with address: " + subnetAddr);
            for (int j = 0; j < 30; j++) {
                int host = (int)(Math.random()*255)+1;
                IPAddr hostAddr = new IPAddr("138.253."+subnet+"."+host);
                if(addressed.contains(hostAddr)) {
                    System.out.println("Host address " + hostAddr + " already exists, skipping.");
                    continue;
                }
                System.out.println("Adding host with address: " + hostAddr);
                sw.addDevice(new PC(hostAddr));
                addressed.add(hostAddr);
            }
        }
    }

    public void simulate() {
        for(int i=0; i< 100; ++i) {
            int dstIndex = (int)(Math.random() * addressed.size());
            IPAddr dst = addressed.get(dstIndex);
            System.out.println("Simulating packet to " + dst);
            String message = "Message # " + i;
            root.receive(new Message(dst, message));
        }
    }
    public static void main(String[] args) {
        IPSwitchDemo demo = new IPSwitchDemo();
        demo.simulate();
    }
}
