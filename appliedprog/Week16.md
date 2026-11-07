# Week 15 A Home-Router

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
        * PortSetter ensures 
        * ARPProtocol
        * IPProtocol
        * ErrorFrameHandler
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

7. DHCP_INFORM we use this to query other parameters, rather than DISCOVER.

