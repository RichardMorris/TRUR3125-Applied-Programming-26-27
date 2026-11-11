# Week 17 The Internet in a box

## User Story

As a teenager I want to chat to my friends over the internet, so I can keep track of all the gossip.

## Acceptance Criteria

Scenario: Router Configuration

* Given: Two Routers connected to TheInternet
* Then: They have different `IPPrefix` like `81.123.45.0/24`in the RIPE NCC `81.0.0.0/8` range (Europe, see [explain xkcd - map of the Internet](https://www.explainxkcd.com/wiki/index.php/195:_Map_of_the_Internet))
* And: A mapping of public IP addresses in the prefix range to private IP addresses in the 192.168.1.0/24 range.

Scenario: PC Configuration

* Given: PC1 connected to a HomeRouter1
* And: PC2 connected to HomeRouter2
* And: The two routers connected to `TheInternet`
* When: Both PC negotiate DHCP
* Then: They are assigned local IP addresses in the 192.168.1.2 to 192.168.1.250 range.
* And: They have subnet masks of 255.255.255.0
* And: The have default gateway addresses of 192.168.1.1
* And: They have public IP addresses assigned by the router.

Scenario: TheInternet configuration

* Given: Two routers connected to the internet.
* Then: It is manually configured to know the IPPrefixes of the routers.
* And: It has the IP address `195.66.226.16` the London Internet Exchange (LINX), the Primary Peering LAN in London.

Scenario: Ping

* Given: PC1 connected to a HomeRouter1
* And: PC2 connected to HomeRouter2
* And: The two routers connected to `TheInternet`
* When: PC1 pings PC2's Public IP address
* Then: The message is sent to HomeRouter1 using the default gateways mac address.
* Then: HomeRouter1 modifies the packet replacing the private IP with the public IP.
* Then: HomeRouter1 send the packet out of port 0, reserved for connection to the internet
* Then: The internet receives the packet and decrements the ttl
* Then: The internet sends to packet to the port with a matching IP Prefix
* Then: HomeRouter2 receives the packet
* Then: HomeRouter2 modifies the packet replacing the public IP with the private IP
* Then: It send the message to the port matching the private IP.
* Then: PC2 receives the message
* And: The TTL has been reduced by 3.
* Then: it replies to the ping
* Then: it passes back through the network
* Finally: PC1 displays the ping reply

  ```text
  PING 81.123.45.2 (81.123.45.2) 56(84) bytes of data.
  64 bytes from 81.123.45.2: icmp_seq=1 ttl=61 time=27.1 ms
  ```

Scenario: traceroute

* Given: Two PC connect via HomeRouters to the internet via buffered connections
* When: PC1 sends a traceroute to PC2 public IP
* Then: The traceroute sequence is completed
* And: PC1 displays

  ```text
  traceroute to 81.123.45.2 (81.95.52.64), 64 hops max
  1   192.168.1.1  2 ticks
  2   195.66.226.16  4 ticks
  3   81.95.52.0  6 ticks
  4   81.123.45.2  8 ticks
  ```

Scenario: IPConfig 

* When: The pc displays its `ipconfig`
* Then: it displays a message like

    ```text
    IPv4 Address. . . . . . . . . . . : 192.168.1.246
    Subnet Mask . . . . . . . . . . . : 255.255.255.0
    Default Gateway . . . . . . . . . : 192.168.1.1
    ```

Scenario: Chat across internet

* Given: Two PC connect via HomeRouters to the internet via buffered connections
* When: Both PC start private chats using each others public IP addresses
* Then: Both PC can send and receive chat message

## Specification

1. A `SubnetMask`
    * Is an `IPAddress` of the subnet mask  
    * Has a `boolean matches(IPAddress src, IPAddress dest)` method that
        * takes the logical AND of the src and dest addresses with the subnet masks
        * tests if the two results are equal.
2. A `IPPrefix`
    * Has prefix `IPAddress` like `138.253.39.0`
    * Has a bit-length like `/24`
    * Has a subnet mask calculated from the prefix and bit length
    * Can be constructed using a string like `192.168.1.0/24`
    * Has a boolean `matches(IPAddress dest)` method that
        * Finds the first bit-length bits of both addresses
        * Returns true if they are equal.
    * There are three prefixes used for local networks
        * Class A `10.0.0.0/8`
        * Class B `172.16.0.0/12`
        * Class C `192.168.0.0/16`
3. A `PrefixDevice`
    * Is an `AddressableDevice`
    * Has and `IPPrefix` with a `getIPPrefix` method.
4. The internet-in-a-box or `TheInternet`
    * Is a `ProtocolDevice`
    * It has a `MultiPortDevice`
    * It has a custom `ARPProtocol` that
        * Has a `put(IPPrefix, MACAddress)` method
        * implements `lookup(IPAddress)` using the `IPPrefix`
        * Is no handlers so never receives ARP messages.
    * It has an `register(PrefixDevice dev,int port)` method that
        * adds mapping between the devices MAC address and port number
        * adds a mapping between the devices IPPrefix and MAC Address.
    * It can deliver the packets using the routing prefix of connected devices

2. A `HomeRouter`
    * Has a `IPPrefix` like `138.253.39.0/24`
    * Has a local IP address `192.168.1.1`
    * Has a `SubnetMask` of `255.255.255.0`
    * Has a routing table than maps local IP address to
    global address in the prefix range.
    * Has 4 ethernet connections to the LAN
    * Has 1 connection to the internet
    * Can assign local IP addresses to devices on the LAN.
    * Forward frames for local IP to the correct port
    * Forwards packets for global IP to the internet
        * The local IP is changed to the corresponding global IP
    * Informs the internet of its IPPrefix
    * Receives packet matching its prefix from the internet and
        * changes the global ip into a local ip
        * forwards the packet to the correct port
