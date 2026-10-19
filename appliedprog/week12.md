# Week 12 towards routers

## User Story

As a home user, I want to connect to a home router and access the internet
so I can send and receive messages from remote locations.

## Acceptance Criteria

Scenario: IP address allocation for local machines

* Given: A network of a PC and a router
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

Scenario: Messages to local printer

* Given: A PC connected to a Printer via a HomeRouter
* When: The PC sends document to the local printer's IP
* Then: It find the printers mac address
* Then: Is sends a series of frames down its connection to the router
* Then: The router forwards the frames using the mac address
* Then: The printer receives the frames and decodes the document
* Then: It stores the document for later recovery

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

1. A `Document` represents a physical document
    * It has a `fileName` 16 bytes
    * It has a `date` 16 bytes
    * It has it's `contents`
2. The User Datagram Protocol `UDPDatagram` is transport layer protocol
    * Its protocol number is 0x11 (17)
    * Its header contains
        * Source Port: 2 bytes
        * Destination Port: 2 bytes
        * Length: 2 bytes
        * Checksum: 2 bytes, unused
    * Its data section contains the contents
3. The internet-in-a-box or `TheInternet`
    * It can receive IP packets
    * It can deliver the packets using the routing prefix of connected devices
4. A `Printer`
    * has a local or global IP address
    * Can receive frames or packets directly.
    * Can receive UDP messages on port 42
    * Documents are decoded and stored.
    * Documents can be retrieved.
5. A `InternetSender` that
    * Sends packets to the internet.
6. A `PC`
    * Can be configured to send packet either to an Ethernet connection, or to an InternetSender.
    * Has a `print(Document,IP)` method that
        * Creates a UDP packet with the document
        * The packet has port 42
        * Creates a IPPacket from the UDP packet
        * Sends the packet to its packet sender.  
7. A `HomeRouter`
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
8. Dynamic Host Configuration Protocol is a UDP protocol
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
11. A `UDPPacketController`
    * Is a `PacketReceiver`
    * Has a `nextReceiver` field specified in the constructor
    * Has a table linking ports to `Socket`
    * Tests if incoming packets matches the UPD protocol, otherwise forward the packet.
    * Extract a `UDPDatagram`
    * Finds the datagram application
    * Has a `bind(int,IPAddress,Socket)` method.
12. A `Socket` is an interface that can send and receive datagrams
    * Has a `send(Object)` method that
    * Has a `receive(Object)` method
    * Has a `close()` method
13. A `DatagramSocket` is a `Socket` that can send and receive `UDPDatagram`
    * It can be bound to a port by the `UDPPacketReceiver`
    * Has an `Application` that receives decoded objects
    * Has a `send(Object)` method that
        * Packages the object in a `UDPDatagram`
        * Sends the datagram to the bound port on the `UDPPacketController`
    * Has a `receive(Object)` method
        * Passes the object to its application.
14. An `Application` represents a running process.
    * It create a `Socket`
    * It can send messages to the socket.
    * If receives messages from the socket and processes them.
15. A `PrintSpooler` is an application
    * It has a `print(Document,IPAddress)` method, that
        * Creates a socket
        * Binds it to port 42 and the IP address
        * Send the document to the socket
        * Closes the socket
16. A `PrintServer` is an application
    * It creates a socket
    * Binds it to port 42 with its IPAddress.
    * Its `receive` method
        * Unpack the data into a document
        * Stores the document in its `forCollection` list.
17. A `ChatServer` is an application.
    * It creates a socket bound on port 43, with a lan broadcast address
    * It has a `say(String)` method that
        * Sends the message to the socket
    * Its `receive` method displays the message.
