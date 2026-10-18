# Week 8 IPPackets

## User Story

As a network engineer I want to transmit large message broken into frames
over networks that do not always preserve frame order so that end users can
send and receive large documents reliably.

## Acceptance criteria

Scenario: packet fragmentation and recombination

* Given: a network of two PC with a simple connection
* When: a large packet is sent across the network
* Then: the packet broken into fragments
* Then: the packets are converted into frames and send across the network
* Then: the second PC collects the fragments
* Then: the fragments are combined into a single packet
* Then: the packet matches the original packet

## Specification

1. An `IPPacket` contains header and a data section. The header has the format
    * `version` 4 bits with the value 0x4 for IPv4
    * `headerLength` 4 bits with value 0x5 when the options are omitted.
    * `typeOfService` 8 bits, 1 byte, value 0x00, comprising
        * `differentiatedServicesCodePoint` 6 bit, unused, has value 0x00.
        * `explicitCongestionNotification` 2 bit, unused, has value 0x0.
    * `totalLength` 16 bit, 2 bytes, length of header and data sections.
    * `identification` 16 bit, 2 bytes, used for uniquely identifying the group of fragments of a single IP datagram.
    * `flagReserved` 1 bit, 0.
    * `flagDF` 1 bit, don't fragment. 0
    * `flagMF` 1 bit, whether there are more fragments, 1 for more 0 for last fragment.
    * `fragmentOffset` 13 bits, 0 for the first fragment or an un-fragmented datagram, otherwise the offset in 8-byte blocks of the fragment.
    * `timeToLive` 8 bit, 1 byte. Number of hops before the packet is dropped. Each passage through
        a router decreases by 1, when it reaches zero the packet is dropped.
    * `protocol` 8 bit, 1 byte. 0x01 for IMCP, 0x06 for TCP, 17, 0x11 for UDP.
    * `headerChecksum` 16-bit 2 byte. Calculated by taking the 1's complement of the 1's complement sum of the 5 16 bit words in the header.
    * `sourceAddress` 4 byte, source IP address
    * `destinationAddress` 4 byte, destination IP address
    * `options` not used 0 bytes.
2. `OnesComplementNumber` manages calculation:
    * It can be constructed from a short, or two bytes.
    * It has an `OnesComplementNumber add(OnesComplementNumber)` to add two numbers according to the rules of one's complement numbers.
    * It has `add(short)` and `add(byte,byte)` methods.
    * Other method to find the one's complement of a number, the absolute value and conversion to a two's complement number.
3. The one's complement sum of two short values is calculated using, with a wrap-around carry

    ```java
    int sum = (a & 0xFFFF) + (b & 0xFFFF);
    short result = (short) ((sum & 0xFFFF) + (sum >> 16));
    ```

4. `IPPacket` has a method `OnesComplementNumber calculateHeaderChecksum()` than calculates the one complement of the ones complement sum of all fields in the headers, including the `headerChecksum` field.
    * On construction `headerChecksum` is first set to zero, then `calculateHeaderChecksum` is called saving the result in the `headerChecksum` field.
    * To check the header of a packet after transmission `calculateHeaderChecksum` is calculated. This should be `OxFFFF`, negative zero in one's complement representation.
    * A method `boolean isValidChecksum()` can be used the check the checksum.
5. For transmission
    * A `IPPacket` can be converted into an array of bytes, with both
    header and data sections.
    * An `IPPacket` can be created from an array of bytes.
    * The reconstructed packet should have a valid checksum and all fields equal.
    * An `IPPacket` can be wrapped in an `EthernetFrame` sent across a network
        and unpacked to produce an identical packet.

## Packet Fragmentation

1. The `PacketFragmentor` splits a large `IPPacket` into a number of smaller packets.
    * The size of the smaller packets is less than a given size, 140 here, 1500 for ethernet.
    * The size of the data section in the smaller packet is a multiple of 8 for all but the last packet.
    * Each packet is given a `fragmentOffset`, it data section starts at the eight time the `fragmentOffset` position in the original packets data section.
    * The smaller packets have the don't fragment flag set.
    * All but the last packet have the more fragments flag set.
    * All the small packet have the same `identification` number
    * Each time a packet is split a different identification number is used
    * When a packet to be split has length less than the limit it not split and has it identification number set to 0.
2. `PacketReceiver` is an interface with a single method
    * `void receivePacket(IPPacket)` that receives recombined packets.
3. The `FragmentCombiner` reassembles packets.
    * It implements `frameHandler` with a `boolean receiveFrame(EthernetFrame)` method.
    * If the first byte of the frames payload is 0x54 it is treated as an IPPacket
    * Otherwise the method returns false.
    * It has a `PacketReceiver` called each time a recombined packet has been fully reconstructed.
    * The frames payload is converted to an IPPacket
    * If the frame does not have a valid header checksum it is dropped, with an error log.
    * If the destinationIP does not match the host IP it is dropped, with an error.
    * If the packet has identification of 0 and more fragments false, it sent to receiveFrame.
    * Incoming packets are held in a table grouped by identification number and senderIP.
4. There is a check for a complete packet each time a frame is received.
    * One of the matching packet has the `moreFragments` set to false.
    * No other packet  has the `moreFragments` set to false. If so an error is reported.
    * The complete data can be reconstructed from matching packets using their offsets and data.
    * Other fields of the header match. If not an error is reported.
    * If all the above checks pass the packet is reconstructed and sent to the PacketReceiver.
5. There is default `PacketReceive` that logs received packets.
6. A `FrameReorderingConnector` change the order physical frames are transmitted.
7. A network of two PC and one FrameReorderingConnector can successfully send a large packet broken into fragments.

## Design

Not too many design pattern's this week but the design of an IPPacket uses quite a bit binary bit manipulation, with `b << 4` to shift the bit in a byte 4 steps to the left;
`(byte) (shortVal >> 8)` to extract the high order byte from a short,
`(byte) (totalLength & 0xFF)` to extract the low order byte.
`(short) ((highByte << 8) | lowByte & 0xFF)` to create a short from a pair of bytes.

The design of OneComplementNumber might be overblown, it could be replace by a set of static methods acting on short values. It was helpful to use this while testing.

Is it better to store an IPPacket at a set of fields, which are converted to and from
byte arrays as needed, or natively as a byte array, with modified getters returning
field constructed from the bytes?

The PacketFragmentor is relatively simple, splitting a fragment into fragments into
smaller packets that match size requirements. The `FragmentCombiner` make heavy use
of `Map` data structures that are defined in the `java.util` package. A `Map` is
a dictionary, associative array or lookup table.

1. `FragmentCollector` is an inner class that combines fragments with the same source and identification.
2. A `Map<Integer, FragmentCollector>` is used to lookup the `FragmentCollector` by identification number.
3. A `Map<IPAddress, Map<Integer, FragmentCollector>>` is used to look up the
map of `FragmentCollector` by IPAddress.
4. Entries in these maps are created as needed by the `computeIfAbsent` method of `Map`.
5. Hash table implementation are used for the first two map as key order is not important.
6. `FragmentCollector` has a map to order fragments by their offset value.
    * As order is important a `TreeMap` us used `new TreeMap<Integer,IPPacket>`
    * Incoming packets are checked to ensure they have the same IP addresses and identification.
    * The are then added to the map, the tree map ensure they are held in increasing order of their offsets.
    * To check the combined package is complete we iterate of the key-value pairs in the map.
    * A record is kept of the next expected offset, initially zero.
    * The key for the next entry should match the expected offset.
    * If not the check fails and the method returns.
    * If it matches then the offset is incremented by the length of this packets data section, divided by 8.
    * If the last fragment has the more fragments field set to false then check passes.
    * A similar iteration through the map is used to assemble the data section for the recombined package.
    * The package is then assembled and sent to `PackageReceiver`.
7. To test large messages a message listing all the ones complement numbers between two bounds. Here the [iterator](https://refactoring.guru/design-patterns/iterator) design pattern
is used "a behavioural design pattern that lets you traverse elements of a collection without exposing its underlying representation (list, stack, tree, etc.)."
    * The iterator has two methods
    * `boolean hasNext()` that report if more elements are available
    * `OnesComplementNumber next()` returns the next number in the sequence
    * We need a custom iterator as the order of One's complement numbers is different.

## Challenges

1. Allow the `PacketFragmentor` to split packets that are already fragments, ensuring
    that they can still be reconstructed.
