# Week 4 Data Link Layer

## User Story

As a network engineer for a large company with thousands of PC's
I want to set up a local area network using simple level 2 switches and PC's referenced by MAC addresses
so that any message received by any switch is sent to the correct PC and displayed.

## Specification

1. A MAC address consist of 6 octets or bytes.
   * As a MAC address and IPAddr are similar differing only in the number of bytes a common base class AbstractAddress is used.
   * MACAddress can be build from arrays of bytes, unsigned ints and String of the form "00:1A:2B:3C:4D:5E" or "00-1A-2B-3C-4D-5E".
   * The can be printed in the form "00:1A:2B:3C:4D:5E".
2. A `EthernetFrame` consist of:
    * destination mac address
    * source mac address
    * a 2 byte payload length <= 1500 (value > 1536 used by EtherType not implemented)
    * the payload
    * four byte frame check sum (not implemented)
3. Factory methods in the `FrameFactory` class are used create frames from strings.
    * Either a single frame for a string < 1500 bytes.
    * A list of frames for longer strings.
4. A FrameDecoder extracts strings from frames and lists of frames.

## Pattern Usage

A factory method is used to create frames from strings. This allows for future expansion
where we may choose to return a sub-class of `EthernetFrame` that implements the missing features.
It also allows some pre and post processing of the arguments. It helps keep
our implementation of `EthernetFrame` simple and too spec, without imposing higher level
knowledge, like Strings on the implementation.
