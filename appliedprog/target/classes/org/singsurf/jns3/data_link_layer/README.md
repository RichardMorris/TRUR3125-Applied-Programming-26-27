# Week 4 Data Link Layer

## User Story

As a network engineer for a large company with thousands of PC's
I want to set up a local area network using simple level 2 switches and PC's referenced by MAC addresses
so that any message received by any switch is sent to the correct PC and displayed.

## Specification

1. A MAC address consist of 6 octets or bytes.
2. As a MAC address and IPAddr are similar differing only in the number of bytes
a common base class AbstractAddress is used. 
3. MACAddress can be build from arrays of bytes, unsigned ints and String of the form
"00:1A:2B:3C:4D:5E" or "00-1A-2B-3C-4D-5E". 
4. The can be printed in the form "00:1A:2B:3C:4D:5E".