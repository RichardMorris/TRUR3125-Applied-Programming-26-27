# Week 13 Generic Protocols

## User Story

As a software engineer I want to refactor my protocol stack with
a common interface so that it is easier to add new protocols
later.

## Design

A common pattern should now become apparent:

* A protocol such  as ARP, IP, ICMP, and UDP has
  * A container type that hold the type of message that
    * Has fields for each field in the protocol
    * Can be constructed using values for the field
    * Can be queried to extract the values for the field.
    * Can be converted into a byte array
    * Its content can be converted to a byte array
    * Can be constructed from a byte array using a static method
    * static methods to check it can handle data from a lower level protocol
    * static methods to constructed from a lower level protocol
  * classes that receive messages
    * These have a set of handlers each handler
      * May match a higher level protocol or match a subtype of the protocol
      * Checks if message matches
      * handles matching messages
    * Have methods that register, and removes, handlers.
  * classes that send messages
    * These package the message up in a lower level protocol
    * Sends the package

Can we encapsulate this using design patterns or Java language features. Here generics map play a role. The presence of many static methods may be a code smell.

## Generics

Interfaces can be defined like

    ```java
    interface MessageMatcher<T> {
        boolean match(T message);
    }
    ```

using a wild-card type indicator `T`.
Implementing class specify the actual message type

    ```java
    class IPPacketMatcher implements MessageMatcher<IPPacket> {
        public boolean match(IPPacket message);
    }
    ```

## Design idea

Lets see how this maps on to the common patter above.

* A protocol such  as ARP, IP, ICMP, and UDP has - represented by the `Protocol<T,L>` type
  * Has a container type `T`
  * Has a lower level protocol container type `L`
  * A container type that hold the type of message - implements `ProtocolContainer`
    * Has fields for each field in the protocol
    * Can be constructed using values for the field
    * Can be queried to extract the values for the field.
    * Can be converted into a byte array - implements `byte[] getBytes()`
    * Its content can be converted to a byte array - implements `byte[] getContents()`
  * Can construct from a byte array - `T fromBytes()`
  * Can check it can handle data from a lower level protocol `boolean match(L message)`
  * Can be created from a lower level protocol - `T fromContainer(L message)`
  * classes that receive messages - implement `MessageReceiver<T,H>`
    * These have a set of handlers each handler is of type `H` a message handler.
      * May match a higher level protocol or match a subtype of the protocol
      * Checks if message matches - implements `boolean matches(T message)`
      * handles matching messages - implements `T handle(T message)`
        * either return null to indicate no further processing
        * or a, potentially modified, message to be passed to the next handler
    * Have methods that register, and removes, handlers - implements `addHandler(H handler)`
  * classes that send messages - implements `MessageSender<T,L>
    * These package the message up in a lower level protocol - implements `L package()`
    * Sends the package - implements `void send(L message)`

The message handler uses the [template method](https://refactoring.guru/design-patterns/template-method) pattern, with a common algorithm

    ```java
    class MessageDelegator<T extends ProtocolContainer, H extends MessageHandler<T>> 
        implements MessageReceiver<T> {
        List<H> handlers;
        public void receive(T message) {
            for(var h:handlers) {
                if(h.matches(message)) {
                    message = h.handle(message);
                    if(message == null)
                        return;
                }
            }
            Jns3Logger.logError()
        }
        public void addHandler(H handler) {
            handlers.add(handler);
        }
    }
    ```

## Specification

Each protocol has

1. An `Protocol` containing protocol specific information and array of bytes as contents.

    * It has a method `byte[] getBytes()` to get the full data including headers
    * It has a method `byte[] getContents()` to get just the contents.
    * It has a method `boolean isValid()` to check the integrity of the data.

2. A `BaseExtractor<E>` defines methods to work with Encapsulations.

    * A method `E fromBytes(byte[])` to create the Encapsulation from an array of bytes

3. A `Extractor<E,L>` defines method to convert from a lower level protocol to a higher level one

    * A method `E extract(L)` to extract the higher level
    * A method `boolean matches(L)` to test if the data in the lower level protocol matches higher level one.

4. An `Encapsulator<E,H>` defines method convert from a higher level protocol to a lower level one

    * A method `E encapsulate(H)` to create a lower level type to a higher level one

5. A `Receiver<E>` for messages receives

    * A method `void receive(E)`

6. A `Sender<E>` to send messages

    * A method `void send(E)`

7. A `Handler<E>` for classes the attempt to handle incoming messages

    * A method `boolean matches(E)` to check if the message can be handled by the class
    * A method `E handle(E)` that returns
      * `null` if the message has been handled and no further processing is needed
      * The original message, if it can not be handled
      * A modified version of the message, such as rewriting an IP address.

8. A `Delegator<E>` a template method to process inbound traffic

    * It implements `Receiver<E>`
    * It has a list of `Handler<E>`
    * Its `receive` method is

    ```java
    public void receive(E message) {
    for(var handler: handlers) {
        if(handler.matches(message)) {
        message = handler.handle(message);
        if(message==null) {
            return;
        }
        }
    }
    // log error
    }
    ```

## Development

A large refactoring needs a careful plan, especially using generics, which are hard to get right.

1. Perform analysis on the current system, to find the classes and methods used in the actual processing of inbound and outbound message.
2. Develop a set of generic template class, that capture the key parts of the system.
3. Develop a simple set of test classes implementing the templates.
4. Expand the behaviour of test classes to match all current behaviour and expected future behaviour.
5. Replace current components in the system one by one with generic implementations, re-running whole test set as we go.

## Analysis

![current operation](diagrams/week13 analysis.png)
