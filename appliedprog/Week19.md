# week 19 - Transmission Control Protocol

## User Story

As new user of the WWW in the mid 1990's I want
reliable transmission across the internet
so that other users can see my personal website
and read the text in the right order.

## Specification

1. The Transmission Control Protocol is transport layer protocol
    * Its protocol number is 0x06
    * It provides reliable, ordered, and error-checked delivery of a stream of octets (bytes) between applications running on hosts communicating via an IP network.

2. A `TCPDatagram` is contained inside an `IPPacket`
    * Its header contains
        * Source Port: 2 bytes
        * Destination Port: 2 bytes
        * Sequence Number: 4 bytes
        * Acknowledgement Number: 4 bytes
        * Data Offset: 4 bits, size of header in 4 byte words, minimum value 5.
        * Reserved: 4 bits, unused 0.
        * Flags: 8 bit
            * CWR, Congestion window reduced: unused, 0
            * ECN, ECN-Echo: unused, 0
            * URG, Urgent: unused 0
            * ACK, Acknowledgment field is significant: 0 for initial SYN, 1 for subsequent packages.
            * PSH, Push function: set after handshake when data is transmitted.
            * RST, Reset connection: unused? 0
            * SYN, Synchronize sequence numbers: set in first and second stage in handshake
            * FIN, Finished transmission.
        * Window: 2 byte, preferred size for receiving machine, unused
        * Checksum: 2 byte, (sender MUST set it and receiver MUST check it)
        * Urgent Pointer: 2 byte unused
        * Options: variable size, unused
    * The checksum is calculate using a pseudo-header
        * Source IP: 4 bytes
        * Dest IP: 4 bytes
        * Zero's: 1 bytes
        * Protocol number: 1 byte
        * TCP length: 2 bytes

3. An `HTTP GET Request` is an ASCII text encapsulated in a TCPDatagram.
    * The src port is 12345, the dest port is 80
    * The content is

        ```text
        GET /index.html HTTP/0.1
        User-Agent: HTTPClient/0.1
        Accept: text/html
        ```

4. An `HTTP Response` is is an ASCII text encapsulated in (multiple) TCP Datagrams.
    * The src port is 80, the dest port is 12345
    * The content is

        ```text
        HTTP/0.1 200 OK
        Date: Fri, 20 Nov 2026 12:55:02 GMT
        Server: HTTPServer/0.1
        Transfer-Encoding: chunked
        Content-Type: text/html; charset=ASCII
        
        <html>
        <head>
        <title>Home Page</title>
        </head>
        <body>
        ...
        </body>
        </html>
        ```

    * Lines endings are Windows style `\r\n`.

5. The `TCPProtocol`
    * Is an `LowerAdapter<IPPacket>`
    * It is an `UpperSocketPort<TCPDatagram>`
    * It has a `TCPPacketHandler` which
        * that matches incoming packets
        * extract an `TCPDatagram`
        * passes it to an internal set of `ProtocolHandler<TCPDatagram>`.
    * There is a receiver-state associated with each source IP: Close, Listen, SynRec, Established, CloseWait, LastAck.
    * There is a sender-state for each dest IP: Closed, SynSent, Established, FinWait1, FinWait2, TimeWait.
    * Has a `startSyn` method of the sender
        * Changes sender-state to `SynSent`,
        * Calculate it's sequence number
        * Sends a SYN message
    * `SYNHandler` handles the first step in the handshake by the receive
        * Changes receiver-state to `SynRec`
        * Calculates the acknowledge number.
        * Sends a SYN/ACK message
    * `SYN_ACKHandler` handles the second step of the handshake.
        * Changes sender-state to `Established`
        * Sends an ACK message.
        * Chunks the data
        * Stores the chunks and its the total length
        * Queues chunks for later sending, one chunk sent per tick.
    * `ACKHandler` handles the third step in handshake.
        * Changes receiver-state to `Established`
    * `PushSender` on tick, sends the next chunk.
        * Has ACK field set, 
    * `PUSHHandler` handles incoming data with the PUSH,ACK flags.

6. An `HTTPServer` is a `LowerSocketAdapter<TCPDatagram>`
    * It has a handler that checks the message is a GET requests.

## Client-Server states


## Transmission sequence

The sequence of requesting and receiving a webpage from a server.

1. The server opens a `TCPSocket` bound to port 80.
2. The client opens a `TCPSocket` bound to port 12345.
3. 

