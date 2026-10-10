# Week 8 TCP

## User Story

As a network engineer I want to have a reliable method of transmitting data to take account
of lost or corrupted frames so end users have confidence that their messages will get through.

## Specification

1. A `IPPacket` contains and `IPheader` and a data section. The `IPheader` has the format
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

See [Civo ip-packet](https://www.civo.com/learn/ip-packets)