# Week 15 DHCP and the home routers

## User Story

As a home user, I want to connect to a home router and access the internet
so I can send and receive messages from remote locations.

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

Scenario: UDP (from last week)

* Given: The UDP package is converted to use the generic framework
* When: It is register with the network layer protocol
* When: UDP layer test and chat application test are run
  * Then: All tests pass, with some changes in the configuration

Scenario: Negotiation process per protocol

1. PC send a DISCOVER request with its mac address and all ip address 0

    * OP = 1
    * CHADDR = PC's mac address
    * All IP Addresses 0.
    * First option: 0x350101: Option 53 (DHCP Message Type) 1 octet (containing DHCPDISCOVER)
    * Second option: 0x3204c0a80164: Option 50 (Request IP address) 4 octets (containing current IP of client)

2. Router receives stores PC's MAC and IPAddress's in it arp table.
3. Router opens two socket's
    * PC's current IP address
    * Ports 67, 68

4. Router sends an OFFER message
    * Sent to the new socket.
    * OP is 0x02
    * CIADDR is zero
    * YIADDR is calculated by the router in the 192.168.1.0/24 range.
    * SIADDR with the local IP of the server 192.168.1.1
    * GIADDR is zero
    * First option: 0x350102: Option 53 (DHCP Message Type) 1 octet (containing DHCPOFFER)
    * Second option: 0x0104ffffff00: Option 1 (Subnet mask) 4 octets (containing 255.255.255.0)
    * Third option: 0x0304c0A80101: Option: 3 (Router) 4 octets (containing 192.168.1.1)
    * Fourth option: 0x330400015080: Option 51 (Address time) 4 octets (a 86400 second lease time)



Scenario: Chat across local network

* Given: Two PC's connected via a HomeRouter
* And: both PC have negotiated their IP addresses with the router
* When: Both PC start private chats with the other IP addresses 
* Then: Both PC can send and receive chat message


Scenario: Messages to remote printer

* Given: A PC connected to a Printer via a HomeRouter
* When: The PC sends document to the remote printers IP
* Then: It finds the mac address for the default gateway
* Then: It sends the frames to the router
* Then: The router translates the senders IP address
* Then: The router sends the packets to the internet
* Then: the internet sends the packet to the printer
* Then: The printer receives the message
* Then: It stores the message for later recovery

Scenario: Messages to local printer from a remote machine

* Given: A PC connected to the Internet connected to a HomeRouter connected to a local printer.
* When: The PC sends document to the remote printers IP
* Then: It finds the mac address for the default gateway
* Then: It sends the frames to the router
* Then: The router translates the senders IP address
* Then: The router sends the packets to theInternetInABox
* Then: theInternetInABox sends the packet to the printer
* Then: The printer receives the message
* Then: It stores the message for later recovery

## Specification

1. Dynamic Host Configuration Protocol is a UDP protocol
    * Messages are sent on port 68
    * Messages are received on port 67
    * The payload have the format
        * OP - 1 byte op type, 0x01 for discover
        * HTYPE - 1 byte hardware type, 0x01 for ethernet
        * HLEN - 1 byte header length - 0x06
        * HOPS - number of hops, 0x00
        * XID - 4 bytes, 0x3903F326
        * SECS - 2 byte, 0x0000
        * FLAGS - 2 byte, 0x0000
        * CIADDR - Client IP address, 4 byte
        * YIADDR  - Your IP address, 4 bytes
        * SIADDR - Server IP address, 4 bytes
        * GIADDR - Gateway IP address: 4 bytes
        * CHADDR -Client Hardware address, 4 * 4 bytes
        * BOOTP Options - 192 octets all zero, not implemented
        * MAGIC COOKIE - marks end of options 4 byte 0x63825363
        * DHCP Options with first byte for type, second byte to length
            * first option (53,1) - DHCP Message Type - +1 bytes 0x350101, 0x350102, 0x350103,  0x350105
            * second option (1,4) - Subnet mask +4 bytes: 0x0104ffffff00
            * other options ignored, can include information about the Domain name server,
            available routers, Host name, Domain name
    * For DISCOVER requests
        * Frames are sent to the broadcast MAC
        * OP is 0x01
        * All ADDR fields are zero
    * For OFFER replies
        * Frame is sent to the senders MAC
        * OP is 0x02
        * CIADDR is zero
        * YIADDR is calculated by the router in the 192.168.1.0/24 range.
        * SIADDR with the local IP of the server 192.168.1.1
        * GIADDR is zero
    * For REQUEST replies confirming the offer
        * Frames are sent to the broadcast MAC
        * OP is 0x01
        * SIADDR with the local IP of the server who's offer was accepted 192.168.1.1
        * All ADDR fields are zero
    * For DHCPACK replies acknowledging the request
        * information is as in the OFFER, apart from the DHCP Message Type option
2. The internet-in-a-box or `TheInternet`
    * It can receive IP packets
    * It can deliver the packets using the routing prefix of connected devices
3. A `InternetSender` that
    * Sends packets to the internet.
4. A `PC`
    * Can be configured to send packet either to an Ethernet connection, or to an InternetSender.
5. A `HomeRouter`
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
9. A subnet mask
    * Has a host `IPAddress`
    * Has and `IPAddress` of the subnet mask  
    * It has a `boolean matches(IPAddress dest)` method that
        * takes the logical AND of the host and dest addresses with the subnet masks
        * tests if the two results are equal.
10. A `IPPrexix`
    * Has host `IPAddress` like `138.253.39.0`
    * Has a bit-length like `/24`
    * Has a boolean matches(IPAddress dest)` method that
        * Finds the first bit-length bits of both addresses
        * Returns true if they are equal.
