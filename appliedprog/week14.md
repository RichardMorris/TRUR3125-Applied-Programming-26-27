# Converting JNS3 to use generics

## User Story

As a software developer I want to use the generic framework with the current implementation, so that:

* all protocol share a common pattern
* it is easier to add new protocols
* it is easier to configure a PC
* other types of devices like a router can be set up with it own custom logic
* the class diagram looks cleaner

## Acceptance Criteria

Scenario: Data-link layer

* Given: Data link layer is converted to use the generic framework
* When: link layer test are run
  * Then: All tests pass, with some changes in the configuration
  * And: a test protocol can be registered with the data-link layer
* When: A test protocol is registered
  * Then: the inbound logic works
  * And: the outbound logic works

Scenario: Network layer

* Given: Network layer is converted to use the generic framework
When: It is register with the data-link layer
* When: network layer test are run
  * Then: All tests pass, with some changes in the configuration
  * And: a test protocol can be registered with the data-link layer
* When: A test protocol is registered
  * Then: the inbound logic works
  * And: the outbound logic works

Scenario: ICMP

* Given: The ICMP package is converted to use the generic framework
When: It is register with the network layer protocol
* When: ICMP layer test are run
  * Then: All tests pass, with some changes in the configuration

Scenario: UDP

* Given: The UDP package is converted to use the generic framework
When: It is register with the network layer protocol
* When: UDP layer test and chat application test are run
  * Then: All tests pass, with some changes in the configuration

Scenario: Passthrough router

* Given: The Passthrough router is converted to use a custom stack of protocols
* When: All tests are run
  * Then: All test pass

Scenario: Physical Layer

* Given: A generalisation of the frame work is created to work with the `byte[]` data type.
* When: the data link layer is registered with the physical layer
* And: All tests are run
  * Then: all test pass

## Implementation notes

Converting the containers types proved relatively simple. The types converted are

* EthernetFrame
* ARPMessage
* IPPacket
* ICMPMessage
* UDPDatagram - logic for created and validating checksums created.

The process of encapsulating a container in a lower level type is more complex than last week as the creation process typically involves additional data.

Currently we have

1. `ARPMessages` to `EthernetFrames` in `ARPController`

    ```java
    var garpFrame = frameFactory.createEthernetFrame(
        MACAddress.BROADCAST_MAC,
        device.getMACAddress(),
        ARPMessage.ARP_ETHER_TYPE,
        garpMessage.getBytes());
    ```

2. `IPPacket` to  `EthernetFrames` in `EthernetPacketSender`

    ```java
    EthernetFrame frame = frameFactory.createEthernetFrame(
        destMac, sourceMac, 
        p.getBytes());
    ```

3. `ICMPMessage` to `IPPacket` in `PingController` and `PingRequestHandler`

    ```java
    IPPacket packet = new IPPacket(
        IPPacket.IMCP, ttl,
        pc.getIPAddress(), destinationIP, 
        pingRequest.getBytes());
    ```

4. `UDPDatagram` to `IPPacket` in `UDPController`

    ```java
    var packet = new IPPacket(IPPacket.UDP, 64,
        device.getIPAddress(), boundAddress, 
        datagram.getBytes()); 
    ```

5. `PC` exposed some of creation methods used in tests than have been inlined.

6. `FrameFactory` did not add any value, its method have been inlined and the class removed. Its method have been replaced by simple constructors.

    ```java
    EthernetFrame frame = new EthernetFrame(destMac, sourceMac, p.getBytes());
    ```

Extractors are still useful.

1. A generic interface `ProtocolExtractor` is defined

    ```java
    public interface ProtocolExtractor<
        L extends ProtocolContainer, 
        H extends ProtocolContainer> {
            public H extract(L type);
            public boolean isExtractable(L type);
    }
    ```

2. `ARPExtractor` implements an `EthernetFrame` to `ARPMessage` extractor.

3. `IPPackerExtractor` implements and `EthernetFrame` to  `IPPacket` extractor.

4. A generic `ByteConverter` interface is user between the physical and data-link layers

    ```java
    public interface ByteConverter<H extends ProtocolContainer> {
            public H fromBytes(byte[] bytes);
            public byte[] toBytes(H type);
    }
    ```

    With a single realisation `ByteToFrameConverter`.

Senders and Receiver these should be easy to implement with some method renaming.

* There were conflicts with the PC implement both FrameReceiver and PacketReceive. Rather than the IS-A relationship it is now a HAS-A relationship. Method to send/receive frames and packets have been inlined and removed. These did feel a bit odd as their are no corresponding OS system calls.
* PassThrough Router needed some changes as well.

Delegator and Handles are the trickiest parts to change.

1. Data Link layer was relatively painless.
2. For ICMP, the ping request handler can forward the packet. Hence it needs a PsudoHeader and the original packet.
3. UDP delegation uses the bind mechanism so we will not try to change that.
4. For IP delegation: the current system uses Chain of Responsibility. Implementing classes are:
    * IPFragmentHandler starts the call down the chain.
    * ICMPPacketReceiver
    * UDPController
    * DefaultPacketReceiver at end of chain.
5. This was relatively easy, only problems were with some tests, that used low level access. One test was dropped as there was a higher level test of the same behaviour. Other test used some ugly code to find the appropriate handler for checking logged packets.

After the first stage, all 145 test pass with 95% coverage. 