# TRUR3125-Applied-Programming-26-27

## Code for the TRUR3125 Applied Programming unit at Truro and Penwith College

This year we will use design patterns to attempt to simulate network operations.
The Java Network Simulator 3 models the various layers of the network stack,
with faithful representations of the protocols, and a test suite that
model network operations.

Each week is stored in a separate branch in the repo.

* Week 3 - JNS3 and the Imaginary Protocol. The first experimental attempt.
  * See [netswitch](appliedprog/src/main/java/org/singsurf/netsym/netswitch/README.md)
  * Branch [week-3-routing](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/week-3-routing)

* Week 4 - Data Link Layer - basic ethernet frames with MAC address, and a PC that can receive a frame.
  * See [wk4-data-link-layer](appliedprog/Week4.md)
  * Branch [text](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk4-data-link-layer)

* Week 5 - Address Resolution Protocol - allows devices to find each other's mac addresses.
  * See [week 5](appliedprog/Week5.md)
  * Branch [wk5-arp](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk5-arp)

* Week 6 - Hubs and Switches - simple implementation of these devices.
  * See [week 6](appliedprog/Week6.md)
  * Branch [wk6-hubs-and-switches](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk6-hubs-and-switches)

* Week 7 - An improved physical layer, allowing connections that can modify the data passing through.
  * See [week 7](appliedprog/Week7.md)
  * Branch [wk7-physical-layer](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk7-physical-layer)

* Week 8 - Network layer, IPPackets, fragmentation and recombination.
  * See [week 8](appliedprog/Week8.md)
  * Branch [wk8-network-layer](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk8-network-layer)

* Week 9 - Modelling time, allows rate limiting of connections and buffering of connections
  * See [week 9](appliedprog/Week9.md)
  * Branch [wk9-time](https://github.com/RichardMorris/TRUR3125-Applied-Programming-26-27/tree/wk9-time)

* Week 10 - Buffering of output , devices can buffer their output over slow connections.
See [week 10](appliedprog/Week10.md)

* Week 11 - IMCP, allows devices to ping each other.
See [week 11](appliedprog/Week11.md)

* Week 12 - UDP and Sockets. Simple applications.
See [week 12](appliedprog/Week12.md)

* Week 13 - Generics, creating a unified framework
See [week 13](appliedprog/Week13.md)

* Week 14 - Generic refactoring, applying the  framework to the current system
See [week 14](appliedprog/Week14.md)

* Week 15 - DHCP - configures IP address on LAN
See [week 15](appliedprog/Week15.md)

* Week 16 - HomeRouter- modifying the protocol framework for a new type of device.
See [week 16](appliedprog/Week16.md)
