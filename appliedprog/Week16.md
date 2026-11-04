# Week 15 The Internet in a box

## User Story

As a home user, I want to connect to a home router and
automatically set up IP addresses
so I can send and receive messages to local network machines
and not have to worry about details like IP address conflicts,
whatever that means.

## Acceptance Criteria

Scenario: IP address allocation for local machines

* Given: A network of a PC and a HomeRouter
* When: The PC is connected
* Then: The PC requests IP address information from the router
* Then: The router supplies the information
* And: allocates a remote IP address for the machine
* When: The pc displays its ipconfig
* Then: it displays a message like

    ```text
    IPv4 Address. . . . . . . . . . . : 192.168.1.246
    Subnet Mask . . . . . . . . . . . : 255.255.255.0
    Default Gateway . . . . . . . . . : 192.168.1.1
    ```

Scenario: Chat across local network

* Given: Two PC's connected via a HomeRouter
* And: both PC have negotiated their IP addresses with the router
* When: Both PC start private chats with the other IP addresses
* Then: Both PC can send and receive chat message

## Specification

See [RFC 1122: STD 3: Requirements for Internet Hosts - Communication Layers](https://www.rfc-editor.org/info/rfc1122/)

1. The internet-in-a-box or `TheInternet`
    * It can receive IP packets
    * It can deliver the packets using the routing prefix of connected devices
2. A `InternetSender` that
    * Sends packets to the internet.
3. A `PC`
    * Can be configured to send packet either to an Ethernet connection, or to an InternetSender.
4. A `HomeRouter`
    * Has a `IPPrefix` like `138.253.39.0/24`
    * Has a local IP address `192.168.1.1`
    * Has a `SubnetMask` of `255.255.255.0`
    * Has a routing table than maps local IP address to
    global address in the prefix range.
    * Has 4 ethernet connections to the LAN
    * Has 1 connection to the internet
    * Can assign local IP addresses to devices on the LAN.
    * Forward frames for local IP to the correct port
    * Forwards packets for global IP to the internet
        * The local IP is changed to the corresponding global IP
    * Informs the internet of its IPPrefix
    * Receives packet matching its prefix from the internet and
        * changes the global ip into a local ip
        * forwards the packet to the correct port
5. A subnet mask
    * Has a host `IPAddress`
    * Has and `IPAddress` of the subnet mask  
    * It has a `boolean matches(IPAddress dest)` method that
        * takes the logical AND of the host and dest addresses with the subnet masks
        * tests if the two results are equal.
6. A `IPPrexix`
    * Has host `IPAddress` like `138.253.39.0`
    * Has a bit-length like `/24`
    * Has a boolean matches(IPAddress dest)` method that
        * Finds the first bit-length bits of both addresses
        * Returns true if they are equal.

7. DHCP_INFORM we use this to query other parameters, rather than DISCOVER.
