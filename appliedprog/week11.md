# DMCP, Ping and Traceroute - chain of responsibility

## User Story

As a network engineer I want to communicate information about the state of the network so I can configure the routers to find the best route to transfer end users data.

## Specification

1. The  Internet Control Message Protocol (ICMP) is a protocol operating in the network layer, to send error messages and operational information about the state of the network.
     * A `ICPMmessage` is encapsulate in an `IPPackage` with protocol 0x01.
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
     * a `handleICMP(IPPacket,ICMPMessagae)` method.
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
     * ARP messages are handled.
     * Only frames with IPPackets are allow otherwise an error is logged.
     * The Time-To-Live of the packet is decrements
     * If the TTL is zero,
          * If the packet is an IMCP message replies to the sender with a `ICMPError`.
          * Other the packet is dropped. (logging error message?)
     * If the packet contains an ICMPMessage
          * If it is a ping request and the IPAddress matches respond with an `ICMPRely`
     * Otherwise the packet is passed to the other port.
13. A PC 
     * Can send an ping request, recording the time of the request
     * Responds to ping request with a `PingReply`
     * Responds to a ping reply
          * If the reply matches a previously sent request
          * Prints a message with the elapse time.
