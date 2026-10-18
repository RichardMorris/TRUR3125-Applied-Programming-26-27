# week 6 - hubs and switched

## User Story

As a network engineer for a small company with multiple PC
I want to set up a network using hubs and switches so that
any PC can send a frame to any other PC on the network.

## Acceptance criteria

Scenario: arp initialisation of multiple PC's connected by a hub
Given: three PC's connected by a hub, that do not know each others MAC addresses
When: one pc tries to send a message to another IP address
Then: the arp table is consulted and does not find the MAC address
Then: and arp request is sent to the hub
Then: the arp request is forwarded to the other PC's
Then: the requested PC replies with an arp response
Then: the hub forward the reply to all other PC's
Then: the original PC updates its arp table.
And: other PC have their arp tables updated

Scenario: sending messages across network of 3 PC's connected by a hub
Given: three PC's connected by a hub, that know each others MAC addresses
When: one pc tries to send a frame to another IP address
Then: the frame is sent to the hub
Then: the frame is forwarded to the other PC's
Then: the requested PC receives and processes the frame

Scenario: sending messages across network of 3 PC's connected by a switch
Given: three PC's connected by a hub, with ARP initialisation
When: one pc tries to send a frame to another IP address
Then: the frame is sent to the hub
Then: the frame is forwarded to the correct PC's
Then: the requested PC receives and processes the frame

## Specification

1. Hubs and Switched both have a number of Ports
2. A `Port` is a type of `Device`
    * It has a unique identification number.
    * Its `getName()` method returns the name of the host device and id number.
    * Its `getMACAddress()` and `getIPAddress()` methods report the value of the host device.
    * It has a `Connection` to one other device
    * The host device calls it `sendFrame(EthernetFrame)` method which
        send frames via the connection
    * When it receives a frame this is passed to the host device
    * If no device is connected then sendFrame logs an error.
3. Hubs and Switches both implement `MultiPortDevice` that
    * Has a fixed number of Ports
    * Has a `receiveFrame(int portNumber, EthernetFrame frame)` that
        receives a frame from a given port.
4. A `Hub` is a simple MultiPortDevice
    * It has a name used solely for reporting purposes.
    * It has an IPAdress and MACAddress but these are never used.
    * Its `receiveFrame` method sends the message every other Port's `sendFrame` method
    * It has no ARPConnection
    * It cannot initiate its own messages
    * Messages sent to its own MACAddress are recorded with an Error in the log.
5. A `Switch` is a MultiPortDevice
    * It has a name, IPAddress and MACAddress
    * It has a table the records the MACAddress of devices connected to each port.
    * Its `receiveFrame(int portNumber, EthernetFrame frame)` method:
        * If the destination address matches a known MACAddress, send the frame to the corresponding port.
        * If the destination address is unknown send the message to all other ports.
        * If the destination address is the broadcast MACAddress send the message to all other ports.
        * It checks that the source MACAddress matches the entry for that port. If it does not match update the table.  
    * It has no ARPConnection
    * It cannot initiate its own messages
    * Messages sent to its own MACAddress are recorded with an Error in the log.
