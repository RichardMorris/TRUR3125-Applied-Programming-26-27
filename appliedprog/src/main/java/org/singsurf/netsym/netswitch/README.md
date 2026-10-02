# week 3 - Imaginary Protocol Switch Routing

## User Story

As a network engineer for a large company with thousands of PC's 
I want to set up a network using simple switches
so that any message received by the entry point switch is sent to
the correct PC based on its IP address and displayed.

## Specification

1. An `IPAddr` is represent as an array of four bytes
   * It can be constructed from an array of bytes, and array of ints
      or a string
   * Individual bytes can be found
   * It can be converted to human readable string repesentation
2. A `Message` consists of an IP address and a String that contains the content of message. 
3. A `Device` has 
   * a fixed IP address
   * a `receive(Message)` method that takes message. 
4. A `PC` is a type of device:
   * It's `receive` method simply displays it's IP address and the contents of the message.
   * If the IP address of the message does not match its own IP address an error message is printed. 
5. A `Switch` is a type of device:
   * It can have upto 255 other devices added via an `add(Device)` method.
   * It has a `bitmask` parameter, that controls which portion of the IP address is used to route the message. The bit mask can be either 8, 16, 24 or 32. 
     * For bitmask == 8 the first byte is used for switching
     * For bitmask == 16 the second byte is used for switching
     * For bitmask == 24 the third byte is used for switching
     * For bitmask == 32 the fourth byte is used for switching
   * When the switching byte is 0 this refers to the switch itself.
   * All devices added must match the higher order bytes, (before the switch byte), of the switches' IP address. 
   * A switches `receive` message will:
      * If the switch byte is 0 print a diagnostic method.
      * If the switch byte matches that of an added device the message is sent to that device
      * If the switch byte does not match then an error message is printed.

## Design Patterns 

