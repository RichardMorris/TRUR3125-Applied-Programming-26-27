package org.singsurf.jns3.hardware;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;

/**
 * A YRouter is a device that has three ports: one leg port and two branch ports (left and right).
 * It receives frames on the leg port and forwards them to the left or right branch ports in a round-robin fashion.
 * It also receives frames on the left and right branch ports and forwards them to the leg port.
 * For a 3-5 YRouter, it will forward 3 frames to the left port and 5 frames to the right port in a cycle.
 */
public class YRouter extends MultiPortDevice {
	static final int LEG_PORT = 0;
	static final int LEFT_PORT = 1;
	static final int RIGHT_PORT = 2;
	
	final int numLeft;
	final int numRight;
	final int numCycle;
	int currentCycle = 0;
	
	public YRouter(String name, int numLeft, int numRight) {
		super(name, 3);
		this.numLeft = numLeft;
		this.numRight = numRight;
		this.numCycle = numLeft + numRight;
	}

	@Override
	public void receiveFrame(int portNumber, EthernetFrame frame) {

		if (portNumber == LEG_PORT) {
			int index = currentCycle % numCycle;
			if (index < numLeft) {
				sendFrame(LEFT_PORT, frame);
			} else {
				sendFrame(RIGHT_PORT, frame);
			}
			currentCycle++;
		} else if (portNumber == LEFT_PORT || portNumber == RIGHT_PORT) {
			// Frame received on left or right ports, send to the leg port
			sendFrame(LEG_PORT, frame);
		} else {
			Jns3Logger.log(name, Jns3Logger.LogLevel.ERROR,"received frame on invalid port: " + portNumber);
		}
	}

}
