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

Interfaces can be defined like

    ```java
    interface MessageMatcher<T> {
        boolean match(T message);
    }
    ```

and implementing class specify the actual message type

    ```java
    class IPPacketMatcher implements MessageMatcher<IPPacket> {
        public boolean match(IPPacket message);
    }
    ```

We look at this major refactoring in the next week.

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
