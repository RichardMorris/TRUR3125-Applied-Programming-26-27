# IMCP, Ping and Traceroute - chain of responsibility

## User Story

As a network engineer I want to
check how long it takes a message to travel across a network
so that I can configure the routers to find the best route to transfer end users data.

## Acceptance criteria

Scenario: two PC's pinging each other

* Given: A network of two PC's connected by a buffered connection
* When: One PC sends an ping request to the other
* Then: a message like "PING 106.8.139.92 56 bytes of data." is displayed
* Then: The second PC replies to the request
* Then: The first PC displays a message like "64 bytes from 106.8.139.92: ttl=47 time=2 tick"

Scenario: two PC's and a router pinging each other 

* Given: A network of two PC's connected by a PassthroughRouter by buffered connections
* When: One PC sends an ping request to the other
* Then: a message like "PING 106.8.139.92 56 bytes of data." is displayed
* Then: The router forwards the request
* Then: The second PC replies to the request
* Then: The router forwards the reply
* Then: The first PC displays a message like "64 bytes 
from 106.8.139.92: ttl=47 time=4 tick"

Scenario: sending a traceroute request

Given: A network of two PC's connected by a PassthroughRouter by buffered connections
When: One PC sends an traceroute request to the other
Then: a message like "traceroute to 192.168.217.188, 64 hops max" is displayed
Then: The PC sends a ping request with ttl 1.
Then: The router decrements the ttl
Then: The router replies with a TTLExpired
Then: The first PC displays a message like "1  192.168.112.118 2 ticks"
Then: The PC sends a ping request with ttl 2.
Then: The router decrements the ttl
Then: The router forwards the message
Then: The second PC replies to the request
Then: The router decrements the ttl
Then: The router forwards the reply
Then: The first PC displays a message like "2  192.168.57.39   4 ticks"

## Specification

1. The  Internet Control Message Protocol (ICMP) is a protocol operating in the network layer, to send error messages and operational information about the state of the network.
     * A `IMCPMessage` is encapsulate in an `IPPackage` with protocol 0x01.
     * It has a 8-byte header
          * `type` - 1 byte for the type of the ICMP message.
          * `code` - 1 byte for the sub-type of the ICMP message.
          * `checksum` - 2 byte One's complement checksum of headers and data.
          * `rest` - 4 bytes, depends on the type of message
     * It had a data section with a maximum of 579 bytes.
2. Specific ICMP type and codes include
     * 0, 0: echo replies to ping
     * 3, 0-15: destination unreachable for a variety of reasons
     * 5, 0-3: redirect messages
     * 8, 0: echo requests, used for ping
     * 9, 0: router advertisement
     * 10, 0: router solicitation
     * 11, 0: time to live exceeded
     * 12, 0-2: bad IP header
     * 13, 0: timestamp
     * 14, 0: timestamp reply
3. For each implemented type and code there is a `ICMPHandler` these have
     * a `handleICMP(IPPacket,ICMPMessage)` method.
     * a `boolean matches(type, code)` method to test if handlers matches the message.
4. Chain of responsibility is used for `PacketReceivers` each receiver
     * either handle the message
     * or calls the next `PacketReceiver` specified by a constructor.
5. The `DefaultPacketReceiver` is the last receiver in each chain.
     * It logs the message.
     * It has no chained receiver.
     * It can be configured to log the message as an ERROR.
6. The `ICMPPacketReceiver` handles ICMP packets. It
     * Tests is the protocol matches 0x01.
     * If not the packet is passed to the next `PacketReceiver`
     * Creates an `ICMPMessage`
     * Loops thorough it's list of `ICMPHandler`
     * Test if the handler's `matches(type,code)`
     * If so calls the `handleICMP(ICMPMessage)`
     * If no handler matches, it logs an error.
7. A `PingRequest` is a `ICMPMessage` with
     * Type 0x08
     * Code 0x00
     * The rest section containing
          * `identifier` 2 bytes: specified by constructor
          * `sequenceNumber` 2 bytes: unique number for each ping
8. A `PingReply` is a `ICMPMessage` with
     * Type 0
     * Code 0
     * rest section matching the original message
9. An `IMCPError` is an `ICMPMessage` with
     * Type 11
     * Code 0
     * rest section matching the original message
     * Data section containing:
          * The IP header of sending request
          * The first 8 bytes of the data section. For TCP this include the port number.
10. The `PingRequestHandler` handles ping requests. It
     * checks if the message has type 8.
     * If so creates a `IMCPMessage` with
          * type 0x00, code 0x00, rest 0x0000, and no data section.
     * Creates an `IPPacket` with a `PingReply` message
          * protocol 0x01
          * Destination IP with Source IP of the packet.
          * Data section with the `PingReply`
     * Sends the packet
11. The `PingReplyHandler` logs replies to ping request. It
     * checks if the message has type 0.
     * Prints the message with the sender IP.
     * Logs the message.
12. The `PassThroughRouter` is a simple router.
     * has an `IPAddress`
     * has 2 ports
     * ARP messages are forwarded.
     * Only frames with IPPackets are allow otherwise an error is logged.
     * The Time-To-Live of the packet is decrements
     * If the TTL is zero,
          * If the packet is an IMCP message replies to the sender with a `ICMPError`.
          * Other the packet is dropped. (logging error message)
     * Otherwise the packet is passed to the other port.

## Design

Handling logic for incoming frames is now getting quite complex:

* Check the frame is valid - data link layer
* Check if it and arp frame - data link layer
  * Handle requests and replies
* Check if it an IP packet if so - network layer
  * Check it is a valid IP packet - network layer
  * Decrement the ttl
  * Handle for expired ttl
  * Check if it is an ICMP if so - IMCP
    * Check it has a valid checksum
    * Check if is a ping request
    * Check if it is a ping reply
    * Check if it if ttl expired

Different strategies are used at the different layers:

1. The data link layer uses a list of FrameHandlers

     ```java
     public void receiveFrame(EthernetFrame frame) {
        for (FrameHandler handler : frameHandlers) {
            if (handler.handleFrame(frame)) {
                return;
            }
        }
        // log error
     }
     ```

2. The network layer uses [chain or responsibility](https://refactoring.guru/design-patterns/chain-of-responsibility), each element in the chain implements `PacketReceiver`. All but the last have a `nextReceiver` field.

     ```java
     public void receivePacket(IPPacket packet) {
          if(packet.getProtocol() != 1) {
               nextReceiver.receivePacket(packet);
               return;
          }
          ...
     }
     ```

   This design allows receivers to modify packet before passing them on. For instance a receiver could decrement the ttl field. Processing is completed
   by calling the first element in the chain.

3. ICMP packets use another iterator, handlers have two methods: `boolean matches(ICMPMessage message)` to check if the message can be handled and a second method `void handle(IPPacket packet, ICMPMessage message)` to actually handle the message.

     ```java
     for (ICMPMessageHandler handler : handlers) {
          if (handler.matches(message)) {
               handler.handle(packet, message);
               return;
          }
     }
     ```

4. The `PassthroughRouter` uses a long set of if statements
and could be considered and example of a [code smell](https://refactoring.guru/smells/long-method). Here the class is used for one specific purpose and is not
expected to be extended or customised. When we come to a full router, a more flexible method can be used.

5. Encapsulation limiting the exposure is achieved using various static methods `PPacket.isIPPacket(payload)`, `ICMPMessage.isICMPMessage(packet)`

6. Using sub-classes for different type of ICMP message
was dropped, instead static factories are used
to create the different types
 and instance methods like `icmp.isPingRequest()`
 determine their types.

7. The calling PC responds to ping and traceroute replies by comparing the reply with the original request stored in the table.
