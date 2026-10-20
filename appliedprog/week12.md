# Week 12 UDP, ports and sockets

## User Story

As a family, we want to chat each other via our PC on a local network 
so we are kept up to date on everyone's messages.

## Acceptance Criteria

Scenario: Document to local printer

* Given: A network of 2 PC's connected to a `Switch`
* When: The first PC starts the `ChatApplication`
* Then: The application creates a `DatagramSocket`
* Then: It binds the Socket to port 42 and multicast IP address on its `UDPController`
* When: The second PC starts the `ChatApplication`
* Then: The application creates a `DatagramSocket`
* Then: It binds the Socket to port 42 and multicast IP address on its `UDPController`
* When: The firstPC `says` a message
* Then: It creates a `UDPDatagram`
* Then: It sends the datagram to the socket
* Then: The UDP controller packages the datagram in an `IPPacket`
* Then: Breaks the packet into frames with the broadcast MAC and sends them to the `EndPoint` and across the network
* Then: The switch broadcasts the frames to the other machines
* Then: The second PC receives the frames
* Then: It recombines the frames into an IPPacket and send down its chain of responsibility
* Then: Its UDP controller receives the packet and extracts the `UDPDatagram`
* Then: It buffers datagrams for that port
* When: The second chat application receives a `tick`
* Then: The it calls the `poll` method of the socket
* Then: It receives the datagram and displays the content

## Specification

1. The User Datagram Protocol is transport layer protocol
    * Its protocol number is 0x11 (17)
    * It is connection less, unreliable, not ordered, and fast.
2. A `UDPDatagram` is contained inside an `IPPacket`
    * Its header contains
        * Source Port: 2 bytes
        * Destination Port: 2 bytes
        * Length: 2 bytes
        * Checksum: 2 bytes, unused
    * Its data section contains the contents
3. A `UDPPacketController`
    * Is a `PacketReceiver`
    * Has a `nextReceiver` field specified in the constructor
    * Has a table linking ports to `Socket`
    * Tests if incoming packets matches the UPD protocol, otherwise forward the packet.
    * Extract a `UDPDatagram`
    * Finds the datagram application
    * Has a `bind(int,IPAddress,Socket)` method.
4. A `DatagramSocket` can send and receive `UDPDatagram` messages.
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
5. A `ChatServer` is an application.
    * It creates a socket bound on port 42, and the LANS broadcast address `192.168.1.255`
    * It has a `say(String)` method that
        * Sends the message to the socket
    * Its `tick` method
        * repeatedly poll's the socket for a datagram
        * If a datagram is received displays the message it contains

## Design

A common pattern should now become apparent:

* A protocol such  as ARP, IP, ICMP, and UDP has
  * A container type that hold the type of message that
    * Has fields for each field in the protocol
    * Can be constructed using values for the field
    * Can be queried to extract the values for the field.
    * Can be converted into a byte array
    * Its content can be converted to a byte array
    * Can be constructed from a byte array using a static method
    * static methods to check it can handle data from a lower level protocol
    * static methods to constructed from a lower level protocol
  * classes that receive messages
    * These have a set of handlers each handler
      * May match a higher level protocol or match a subtype of the protocol
      * Checks if message matches
      * handles matching messages
    * Have methods that register, and removes, handlers.
  * classes that send messages
    * These package the message up in a lower level protocol
    * Sends the package

Can we encapsulate this using design patterns or Java language features. Here generics map play a role.
Interfaces can be defined like

    ```java
    interface MessageMatcher<T> {
        boolean match(T message);
    }
    ```

and implementing class specify the actual message type

    ```java
    class IPPacketMatcher implements MessageMatcher<IPPacket> {
        public boolean match(IPPacket message);
    }
    ```

We look at this major refactoring in the next week.

* A protocol such  as ARP, IP, ICMP, and UDP has - represented by the `Protocol<T,L>` type
  * Has a container type `T`
  * Has a lower level protocol container type `L`
  * A container type that hold the type of message - implements `ProtocolContainer`
    * Has fields for each field in the protocol
    * Can be constructed using values for the field
    * Can be queried to extract the values for the field.
    * Can be converted into a byte array - implements `byte[] getBytes()`
    * Its content can be converted to a byte array - implements `byte[] getContents()`
  * Can construct from a byte array - `T fromBytes()`
  * Can check it can handle data from a lower level protocol `boolean match(L message)`
  * Can be created from a lower level protocol - `T fromContainer(L message)`
  * classes that receive messages - implement `MessageReceiver<T,H>`
    * These have a set of handlers each handler is of type `H` a message handler.
      * May match a higher level protocol or match a subtype of the protocol
      * Checks if message matches - implements `boolean matches(T message)`
      * handles matching messages - implements `T handle(T message)` 
        * either return null to indicate no further processing
        * or a, potentially modified, message to be passed to the next handler
    * Have methods that register, and removes, handlers - implements `addHandler(H handler)`
  * classes that send messages - implements `MessageSender<T,L>
    * These package the message up in a lower level protocol - implements `L package()`
    * Sends the package - implements `void send(L message)`
