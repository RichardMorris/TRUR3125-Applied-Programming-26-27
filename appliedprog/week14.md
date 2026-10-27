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
