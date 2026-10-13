# week 9 - time

## User story

As a network engineer working with a wide area network where packets my take different routes and experience delays I want to combine out of sequence fragments on the receiving machine so that large messages can reliably be transmitted  long distances.

## Specification

1. Time is modelled using a `GobalWallClock`
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
	* It is determistic in operation.
3. The `BufferedConnection` is a connection that buffers it output
	* When a frame is received by it `send` method it is stored 
		in a buffer, but not transmitted.
	* When the connection receives the `tick` message from the clock
		all frames in the buffer are sent to its receive message, and the buffer
		is cleared. 
4. A network can be constructed consisting of two PC's, and two YRouters
	* The PC are connected to the legs of the YRouters
	* The arms of the YRouter are connected to each other
	* One with a SimpleConnection
	* One with a BufferedConnection
	* Fragmented Packets can be sent across the network and successfully recombined. 
	
## Design

How to model time has been in the back of my mind since week 7 the physical layer.
The first though was to use threads in some way. Currently all parts of the system work in a single thread, so as soon as one PC sends a message, the other PC receives a message.
Several option were considered:

1. Each PC runs in a single thread, EndPoints can be used to manage communication between threads.
2. Connections could use threads internally   

There are many options for threaded communication in Java, you can create a simple `Runnable` object with a `run()` method, and then create a `Thread` with the Runnable as  arguemnen. Finally call the `Thread` `start()` method. 