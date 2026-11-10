# Week 16 A Home-Router

## User Story

As a home user, I want to connect to a home router and
automatically set up IP addresses
so I can send and receive messages to local network machines
and not have to worry about details like IP address conflicts,
whatever that means.

## Acceptance Criteria

Scenario: IP address allocation for local machines

* Given: A network of a PC and a `HomeRouter`
* When: The PC is connected
* Then: The PC requests IP address information from the router
* Then: The router supplies the information
* And: allocates a remote IP address for the machine
* When: The pc displays its `ipconfig`
* Then: it displays a message like

    ```text
    IPv4 Address. . . . . . . . . . . : 192.168.1.246
    Subnet Mask . . . . . . . . . . . : 255.255.255.0
    Default Gateway . . . . . . . . . : 192.168.1.1
    ```

Scenario: Chat across local network

* Given: Two PC's connected via a `HomeRouter`
* And: both PC have negotiated their IP addresses with the router
* When: Both PC start private chats with the other IP addresses
* Then: Both PC can send and receive chat message

## Specification

See [RFC 1122: STD 3: Requirements for Internet Hosts - Communication Layers](https://www.rfc-editor.org/info/rfc1122/)

1. A `HomeRouter`
    * Has a local IP address `192.168.1.1`
    * Has a `SubnetMask` of `255.255.255.0`
    * Has an allocation table of allocated local IP addresses matching the subnet mask.
    * Has 4 ethernet connections to the LAN
    * Can assign local IP addresses to devices on the LAN via DHCP ensuring no conflicts
    * Has separate MAC addresses for each port.
    * For ARP
        * Responds to an ARP request for it's IP, with the MAC address of the port it was called from.
        * Forwards ARP requests to other machines to the corresponding port if known, or Broadcast if unknown.
        * Forwards ARP replies to correct machine.
    * For IP packets
        * Decrements the `ttl`
        * If the `ttl` is zero reply with a "TTL Expired" ICMP message.
        * Looks up the destination IP in its table
        * If not found reply with a "Destination unreachable" message.
        * If found forwards to the correct port.
        * If the destination IP matches this machine delegate to appropriate `PacketHandlers`
    * For ICMP packets
        * If its a ping request to this machine respond with a ping reply.
        * Otherwise as for general IP packets
    * For non IP message
        * Respond with "Destination protocol unreachable"
    * For UDP packets to this machine or the limited multicast address
        * Forward to the correct socket
    * For UDP packets to unknown port
        * Respond with "Destination port unreachable"
    * For DHCP packets to port 67 DHCP Server
        * Delegate to DHCPProtocol.
        * Respond to DISCOVER with OFFER
        * Respond to REQUEST with ACK
        * Respond to INFORM with ACK with appropriate options

2. A `HomeRouter`
    * Has a port mapping [Port Number: List of Mac Addresses]
    * Has an APR Table [IP Address: Mac Address]
    * Has an array of ports indexed by `portNum`
    * Messages received on port `portNum` are send to the method
    `receive(portNum, EthernetFrame)`.
        * The port mapping table is updated
        * It is passed to `receive(EthernetFrame)` of the router's `EthernetProtocol`.
    * Outbound messages are delivered to the `EthernetSender.send(EthernetFrame)` method.
        * Frames to a specified `Dest Mac` are delivered to `send(portNum, byte[])`
        * Frames with the broadcast MAC are sent to all ports not matching the `Source MAC`
        * Frames to an unknown MAC are dropped with an error log.

3. The `HomeRouter` class' `EthernetProtocol` sends inbound frames to it's registered protocols/handlers:
    * `ARPProtocol` delegates to
        * `ARPSetter` compare sender MAC/IP with it's table
            * If same continue so it can be forwarded
            * If different, update ARPTable, update port mapping
        * `ARPBroadcastForwarder` forwards broadcast packets to ports not matching sender mac
        * `ARPForwarder`
            * if dest IP is in table forward to corresponding hardware port
            * if dest mac is not in table forwards to all packets not matching sender mac
        * `ARPResponder`
            * if  

4. A subnet mask
    * Is an `IPAddress`
    * Has a `boolean matches(IPAddress src, IPAddress dest)` method that
        * takes the logical AND of the src and dest addresses with the subnet masks
        * returns true if the two results are equal.

5. The PC
    * "The client SHOULD broadcast an ARP
   reply to announce the client's new IP address and clear any outdated
   ARP cache entries in hosts on the client's subnet."
    * On receiving a DHCP-ACK sends an ARP Reply to the Broadcast MAC address to announce it new IP.
    * On receiving a ARP Reply to Broadcast MAC updates its ARP table.

6. DHCP_INFORM we use this to query other parameters, rather than DISCOVER.

## Design Notes

This week see the conversion of the `HomeRouter` to be a `ProtocolDevice` with a custom set of protocols. Several protocol have been modified to work with routers, and the data-link layer and physical layer modified to work with protocols, via the
`UpperPhysicalPort` and `LowerPhysicalAdapter` classes.

1. ``DHCPProtocol` has an `isServer` option, that changes the set of handlers to `DiscoverHandler`,`RequestHandler` for servers, and `OfferHandler`,`AckHandler` for clients.
2. `UDPProtocol` same for clients and servers
3. `ICMPProtocol` same for clients and servers
4. `IPProtocol` has an `isRouter` option adding a couple of new handlers:
    * `TTLDecrementor` Decrements TTL, and if zero send a DHCP Time Exceeded message to calling device.
    * `IPPacketForwarder` forwards packets to the correct hardware port.
5. `ARPProtocol` has an `isRouter` option adding a new handlers:
    * `ARPForwarder` forwards broadcast packets to other hardware port, as well as handling them by the controller.
6. `EthernetProtocol`  now works with multiple port devices, treating single port machines as a special case.
    * `PortMapper` defines a map between MAC addresses and port numbers with the concrete class `MultiPortMapper`.
    * An anonymous class `MultiPortByteReceiver` extracts EthernetFrames from byte arrays.
    * A `MultiPortFrameReceiver` with a `receive(int portNum, EthernetFrame frame)` that records the port number and passes the frame to the delegator.
    * A `MultiPortFrameSender` that takes outbound frames, directs them to the correct port, and buffers
    if the end point is blocked.

7. `PhysicalProtocol` a new small protocol working with multiple ports.
    * `BlockingEndPoint` only allows outbound traffic if the connection is not blocked.
    * Inbound traffic from the endpoint is sent to the `MultiPortByteReceiver`, with the port number.

8. A `HomeRouter` is a `ProtocolDevice` with this new set of Protocols.

The `HomeRouter` class from the previous week and the `PassthroughRouter` from week 11 server as templates for some of the functionality. The latter had most of the logic in a single method.

To implement the new `HomeRouter` a step by step approach was used. For each layer from the data-link layer up, test are created using existing tests as a basis. The changes to the protocol are implemented. For the data-link layer, a custom `RouterEthernetProtocol` is created.

Once the full stack has been tested, it was time to create a `PhysicalProtocol` with the new Port and Adapter classes. Some reorganisation of classes was necessary
to ensure the protocol only works with byte arrays and not frames. Buffering was moved to the Ethernet protocol.

Finally functionality of the `RouterEthernetProtocol` is
pulled up to the `EthernetProtocol` so a single class is used for PC and routers. Again this is done in a step by step fashion, ensuring all tests are green at each step.

Once done the old HomeRouter and a few low level tests were dropped. Coverage reports also identified some now unused methods and classes that are also dropped.

The inbound logic for the IP layer posed some problems. The `TTLDecrementor` should be run once for each inbound packet, and the `IPFragementHandler` passes completed packets back to the delegator. Both classes could not be implemented as `PacketHandler` in the same delegator loop without decrementing the TTL of a recombined packet twice. The simplest solution was to implement the decrementor as chained method passing modified frames to the delegator.

This now completes the conversion to using Generic and Protocols. The Protocol level view looks quite clean.

![Protocol diagram](diagrams/week16%20protocols.png)

The `PhysicalConnection` - `PhysicalEndPoint` also serve as a port-adapter mechanism between the edge of the device, and connections join it to other devices.

The full system can be viewed as multi-level multiplexing system:

* Inbound data on different ports is converted to frames and multiplex into a single channel
* They are then demultiplex according to the `EtherType` of the frame by the Ethernet protocol's delegator.
* IP packets are demultiplex according to the `protocol` of the packet.
* UDP datagrams are demultiplex according the IP Address and Port number.

For outbound traffic

* Data from different application are multiplex into UDP datagrams
* UDP datagrams and ICMP messages are multiplexed into IP Packets.
* IP packets and APR messages are multiplexed into frames
* Frames are demultiplex according to the mac address of the frame.

## Review

Now the full set of Protocol classes have been created its time to reflect on the changes.

Advantages:

* The protocol view looks cleaner, less of the internal functionality is exposed, and the main PC/HomeRouter classes are quite simple.
* The system should be quite flexible, it should be fairly easy to add a `TCPProtocol` and applications like ftp, http, that require it.
* It should be fairly easy to modify protocols, when we look at connection from the HomeRouter to the internet then we will need a new Port mapping using subnet masks and IP prefix's.
* The delegator/handler structure works for most protocols. It's only the IP part that needed a slightly different logic, and that was easy to implement using the framework.
* The code is largely faithful to the various RFC's.

Disadvantages:

* The code is more complex. In the `PassthroughRouter` much of the logic is contained in a single method. Now its distributed over many classes.
* The stack trace is deep, each step in the transmission add 1 or two stack frames, and it can take 30 frame for a single Chat application packet to pass out from the PC; through a connection and into the HomeRouter; out on a different port; across another connection; and into the internal logic of the receiving PC.
* The code is not efficient. Not really a design goal, but for real systems we would want efficient code that can handle millions of transmissions. This would not be written in Java, that uses garbage collection for freeing memory. Instead a language with manual memory management like C, C++, Rust, or [Zig](https://en.wikipedia.org/wiki/Zig_(programming_language)), would give more predictable performance.

## Class Diagram

Showing classes with substantial modification.

![Class diagram](diagrams/week16.png)
