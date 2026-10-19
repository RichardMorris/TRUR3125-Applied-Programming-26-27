# Week 12 towards routers

## User Story

As a home user, I want to connect to a printer on my home network
so I can send documents to the printer.

## Acceptance Criteria

Scenario: Document to local printer

* Given: A PC connected to a `PrinterServer` via a Switch
* When: The PC sends document a document to the `PrintSpooler`
* And: A `PrintServer` is listening on port 42.
* Then: The `PrintSpooler` creates a `DatagramSocket`
* Then: It binds the Socket to port 42 on the `UDPController`
* Then: It creates a `UDPDatagram`
* Then: Sends the datagram to the socket
* Then: The UDP controller packages the datagram in an `IPPacket`
* Then: Breaks the packet into frames and sends them to the `EndPoint` and across the network
* Then: The printer server receives the frames
* Then: Recombines the frames into an IPPacket that is send down its chain of responsibility
* Then: Its UDP controller receives the packet and extracts the `UDPDatagram`
* Then: It buffers datagrams for that port
* When: The print Server calls `receive()
* Then: The PrintServer decodes the Document and stores the document for later recovery

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
3. A `Printer`
    * has a local or global IP address
    * Can receive frames or packets directly.
    * Can receive UDP messages on port 42
    * Documents are decoded and stored.
    * Documents can be retrieved.
4. A `PC`
    * Has a `print(Document,IP)` method that
        * Creates a UDP packet with the document
        * The packet has port 42
        * Creates a IPPacket from the UDP packet
        * Sends the packet to its packet sender.  
5. A `UDPPacketController`
    * Is a `PacketReceiver`
    * Has a `nextReceiver` field specified in the constructor
    * Has a table linking ports to `Socket`
    * Tests if incoming packets matches the UPD protocol, otherwise forward the packet.
    * Extract a `UDPDatagram`
    * Finds the datagram application
    * Has a `bind(int,IPAddress,Socket)` method.
6. A `DatagramSocket` can send and receive `UDPDatagram` messages.
    * It can be bound to a port by the `UDPPacketReceiver`
    * Has an `Application` that receives decoded objects
    * Has a `send(UDPDatagram)` method that
        * Packages the object in a `UDPDatagram`
        * Sends the datagram to the bound port on the `UDPPacketController`
    * Has a `UDPDatagram receive()` method
        * Passes the object to its application.
        * Returns null if no datagram is available
        * (This conflicts with Java method that blocks until a datagram is available, but does not work with our
        model of time).
    * Has a `close()` method
7. An `Application` represents a running process.
    * It create a `Socket`
    * It can send messages to the socket.
    * If receives messages from the socket and processes them.
8. A `PrintSpooler` is an application
    * It has a `print(Document,IPAddress)` method, that
        * Creates a socket
        * Binds it to port 42 and the IP address
        * Send the document to the socket
        * Closes the socket
9. A `PrintServer` is an application
    * It creates a socket
    * Binds it to port 42 with its IPAddress.
    * Its `receive(UDPDatagram)` method
        * Unpack the data into a document
        * Stores the document in its `forCollection` list.
10. A `ChatServer` is an application.
    * It creates a socket bound on port 43, and the LANS broadcast address `192.168.1.255`
    * It has a `say(String)` method that
        * Sends the message to the socket
    * Its `receive` method displays the message.
