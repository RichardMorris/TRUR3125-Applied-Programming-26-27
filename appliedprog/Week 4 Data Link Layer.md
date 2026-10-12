# Week 4 Data Link Layer

## User Story

As a network engineer for a large company with thousands of PC's
I want to set up a local area network using simple level 2 switches
and PC's referenced by MAC addresses
so that any message received by any switch is sent to the correct PC and displayed.

## Specification

1. A `MACAddress` consist of 6 octets or bytes.
   * As a MAC address and `IPAddr` are similar differing only in the number of bytes a common base class `AbstractAddress` is used.
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
    * it can decode strings from frames and lists of frames.
4. A `DeviceFactory`
    * creates random mac addresses, ensuring no duplicates, and stores known addresses.
    * creates PC with a random mac address.
5. All devices that can receive frames implement `FrameReceiver`.
    * This has a single method `public void receiveFrame(EthernetFrame frame)`
6. The `Jns3Logger` is used to record a log of activity.
7. A `PC` has
    * A random MAC address
    * A unique name
    * It can receive frames and log them via the Jns3Logger.

## Pattern Usage

A factory method

```java
EthernetFrame FrameFactory.createEthernetFrame(MACAddress,MACAddress,String)
```

 is used to create frames from strings. This allows for future expansion
where we may choose to return a sub-class of `EthernetFrame` that implements the missing features.
It also allows some pre and post processing of the arguments. It helps keep
our implementation of `EthernetFrame` simple and to spec, without imposing higher level
knowledge, like Strings on the implementation.

The `DeviceFactory` also uses Factory Methods to create random MAC Address
and PC's. Code inside the factory methods
ensure no two devices have the same address.

The exact constructor used to make a  `PC` changed several times, the factory method hid this change from calling code. It also generates a random
mac address saving the calling code from having to create MAC addresses.
