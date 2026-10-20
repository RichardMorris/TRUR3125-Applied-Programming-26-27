package org.singsurf.jns3.transport_layer.udp;

import org.singsurf.jns3.hardware.GlobalWallClock;
import org.singsurf.jns3.hardware.GlobalWallClock.TickListener;
import org.singsurf.jns3.hardware.PC;
import org.singsurf.jns3.transport_layer.udp.UDPController.DatagramPacket;

public class ChatApplication implements TickListener{
	PC pc;
	private DatagramSocket socket;
	static final short PORT = 42;
	
	public ChatApplication(PC pc) {
		this.pc = pc;
		socket = new DatagramSocket(pc.getName() + "-ChatSocket "+ PORT);
		var multicastAddress = pc.getMulticastAddress();
		pc.bind(socket, multicastAddress, PORT);
		GlobalWallClock.getInstance().addTickListener(this);
	}

	public void say(String message) {
		pc.display(pc.getName() + " says: " + message);
		socket.send(new UDPDatagram(PORT, PORT, message.getBytes()));
	}

	@Override
	public void onTick() {
		DatagramPacket datagram;
		while((datagram = socket.poll()) != null) {
			String message = new String(datagram.datagram().getContents());
			pc.display("" +  datagram.ip() + " says: " + message);
		}
	}
	
	
}
