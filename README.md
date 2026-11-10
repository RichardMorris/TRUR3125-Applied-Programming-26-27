# TRUR3125-Applied-Programming-26-27

## Code for the TRUR3125 Applied Programming unit at Truro and Penwith College

This year we will use design patterns to attempt to simulate network operations.
The Java Network Simulator 3 models the various layers of the network stack,
with faithful representations of the protocols, and a test suite that
model network operations.

Each week is stored in a separate branch in the repo, so the evolution of the program can be observed. Branches are merged into main after each lesson.

* Week 3 - JNS3 and the Imaginary Protocol. The first experimental attempt.
  * See [ipswitch](appliedprog/src/main/java/org/singsurf/jns3/ipswitch/README.md)
  * Branch [week-3-routing](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/week-3-routing)

* Week 4 - Data Link Layer - basic ethernet frames with MAC address, and a PC that can receive a frame.
  * See [Week 4](appliedprog/Week4.md)
  * Branch [wk4-data-link-layer](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk4-data-link-layer)

* Week 5 - Address Resolution Protocol - allows devices to find each other's mac addresses.
  * See [Week 5](appliedprog/Week5.md)
  * Branch [wk5-arp](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk5-arp)

* Week 6 - Hubs and Switches - simple implementation of these devices.
  * See [Week 6](appliedprog/Week6.md)
  * Branch [wk6-hubs-and-switches](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk6-hubs-and-switches)

* Week 7 - An improved physical layer, allowing connections that can modify the data passing through.
  * See [Week 7](appliedprog/Week7.md)
  * Branch [wk7-physical-layer](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk7-physical-layer)

* Week 8 - Network layer, IPPackets, fragmentation and recombination.
  * See [Week 8](appliedprog/Week8.md)
  * Branch [wk8-network-layer](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk8-network-layer)

* Week 9 - Modelling time, allows rate limiting of connections and buffering of connections
  * See [Week 9](appliedprog/Week9.md)
  * Branch [wk9-time](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk9-time)

* Week 10 - Buffering of output , devices can buffer their output over slow connections.
  * See [Week 10](appliedprog/Week10.md)
  * Branch [wk10-throtteling](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk10-throtteling)

* Week 11 - Internet Control Message Protocol ICMP, allows devices to ping each other.
  * See [Week 11](appliedprog/Week11.md)
  * Branch [wk11-IMCP](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk11-ICMP)

* Week 12 - User Datagram Protocol UDP, Ports and Sockets. Simple applications.
  * See [Week 12](appliedprog/Week12.md)
  * Branch [wk12_UDP_port_and_sockets](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk12_UDP_port_and_sockets)

* Week 13 - Generics, creating a unified framework
  * See [Week 13](appliedprog/Week13.md)
  * Branch [wk13_generics](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk13_generics)
  
* Week 14 - Generic refactoring, applying the  framework to the current system
  * See [Week 14](appliedprog/Week14.md)
  * Branch [wk14_generic_refactoring](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk14_generic_refactoring)

* Week 15 - DHCP - configures IP address on LAN
  * See [week 15](appliedprog/Week15.md)
  * Branch [wk15_DHCP](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk15_DHCP)

* Week 16 - HomeRouter- modifying the protocol framework for a new type of device.
  * See [week 16](appliedprog/Week16.md)
  * Branch [wk16_HomeRouter](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk16_HomeRouter)
