package org.singsurf.jns3.transport_layer.udp;

import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class UDPProtocol implements LowerAdapter<IPPacket>{

	UDPController udpController;
	AddressableDevice device;
	
	public UDPProtocol(AddressableDevice device) {
		this.device = device;
	}

	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(udpController);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<IPPacket> receiver) {
	}

	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		udpController = new UDPController(device, (PacketSender) sender);
	}

	public void bind(IPAddress multicastAddress, short portNumber, DatagramSocket socket) {
		udpController.bind(multicastAddress, portNumber, socket);
	}

}
