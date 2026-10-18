# week 9 - time

## User story

As a network engineer working with a wide area network where packets my take different routes and experience delays I want to combine out of sequence fragments on the receiving machine so that large messages can reliably be transmitted  long distances.

## Acceptance criteria

Scenario: fragments sent over different routes

* Given: a network consisting of two PC's, and two YRouters
* And: The PC are connected to the legs of the YRouters
* And: The arms of the YRouter are connected to each other
* And: One with a SimpleConnection
* And: One with a BufferedConnection
* Then: Fragmented Packets can be sent across the network and successfully recombined.

## Specification

1. Time is modelled using a `GlobalWallClock`
    * There is only one instance of the `GlobalWallClock`
    * It is lazily constructed only when needed.
    * Different components can listen to the wall clock receiving
        a `tick` message, to tell them that time has advanced.
    * The components can tack action when receiving the tick message.
    * The global wall clock can be made to `advance` by testing code.
2. The `YRouter` is a simple router with three ports: two arms and a leg.
    * When a frame is receive by the leg it is sent down either arm
    * When a frame is received by an arm it is always send down the leg.
    * The YRouter can be configured to change how often it sends packets down one
        arm or the other.
    * It is deterministic in operation.
3. The `BufferedConnection` is a connection that buffers it output
    * When a frame is received by it `send` method it is stored
        in a buffer, but not transmitted.
    * When the connection receives the `tick` message from the clock
        all frames in the buffer are sent to its receive message, and the buffer
        is cleared.

## Design

How to model time has been in the back of my mind since week 7 the physical layer.
The first though was to use threads in some way. Currently all parts of the system work in a single thread, so as soon as one PC sends a message, the other PC receives a message.
Several option were considered:

1. Each PC runs in a single thread, EndPoints can be used to manage communication between threads.
2. Connections could use threads internally.

There are many options for threaded communication in Java, you can create a simple `Runnable` object with a `run()` method, and then create a `Thread` with the Runnable as  argument. Finally call the `Thread` `start()` method to start the thread running. Better is to use a ExecutorService that is better at reusing threads. Communication between threads can be used by setting atomic variables or through synchronous methods. Mechanism like the Publisher Subscriber, ConcurrentHashMap allow better communication and synchronisation of threads.

Threads have some difficulties. They are an advanced topic beyond the scope of this module. They make it hard to test applications as you can't rely the order that each thread perform its operations.

Using our global wall clock, eliminates some of these problems, it does not require advanced topics, allows good deterministic control while testing, is sufficient for our simulation needs. It also allows an example of the [Singleton](https://refactoring.guru/design-patterns/singleton) pattern.

> A creational design pattern that lets you ensure that a class has only one instance
     while providing a global access point to this instance.

The standard way of implementing this uses a private constructor and a static
`getInstance()` method, that create the singleton instance if needed.

The `tick` method is an simple example of the [Observer](https://refactoring.guru/design-patterns/observer) pattern:

> A behavioural design pattern that lets you define a subscription mechanism to notify multiple objects about any events that happen to the object they’re observing.

Here the `tick()` is the notification sent to subscribers. The

```java
    GlobalWallClock.getInstance().addTickListener(this);
 ```

method can be used to add our subscriber/listener.

The Observer pattern is frequently used in Event driven programming often used in GUI programming
to respond to button click or mouse actions. You may well have used this in JavaScript with the [addEventListener](https://developer.mozilla.org/en-US/docs/Web/API/EventTarget/addEventListener) method. In Java there are classes like the
[MouseListener](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/event/MouseListener.html) class to track mouse events.

Java used to have an [Observable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Observable.html)/[Observer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Observer.html) system, but it had problems with its event model and has been Deprecated and replaced by the more complex
[Flow](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html) publisher/subscriber model.
