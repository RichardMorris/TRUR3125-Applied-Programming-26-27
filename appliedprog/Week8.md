# Week 8 IPPackets

## User Story

As a network engineer I want to transmit large message broken into frames 
over networks that do not always preserve frame order so that end users can 
send and receive large documents reliably.

## Specification

1. An `IPPacket` contains header and a data section. The header has the format
    * `version` 4 bits with the value 0x4 for IPv4
    * `headerLength` 4 bits with value 0x5 when the options are omitted.
    * `typeOfService` 8 bits, 1 byte comprising, value 0x00
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
    * Other method 
3. The one's complement sum of two short values is calculated using, with a wrap-around carry
```
int sum = (a & 0xFFFF) + (b & 0xFFFF);
short result = (short) ((sum & 0xFFFF) + (sum >> 16));
```
4. `IPPacket` has a method `OnesComplementNumber calculateHeaderChecksum()` than calculates the one complement of the ones complement sum of all fields in the headers, including the `headerChecksum` field.
    * On construction `headerChecksum` is first set to zero, then `calculateHeaderChecksum` is called saving the result in the `headerChecksum` field. 
    * To check the header of a packet after transmission `calculateHeaderChecksum` is calculated. This should be `OxFFFF`, negative zero in one's complement representation. 
    * A method `boolean isValidChecksum()` can be used the check the checksum. 