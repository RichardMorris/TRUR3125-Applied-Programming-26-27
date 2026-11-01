package org.singsurf.jns3.transport_layer.udp;

import java.util.List;

import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.LowerSocketAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.UpperSocketPort;
import org.singsurf.jns3.hardware.AddressableDevice;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.PacketSender;

public class UDPProtocol implements Protocol, LowerAdapter<IPPacket>, UpperSocketPort<UDPDatagram> {

	UDPController udpController;
	AddressableDevice device;
	UDPSocketFactory socketFactory;
	
	public UDPProtocol(AddressableDevice device) {
		this.device = device;
		udpController = new UDPController(device);
		socketFactory = new UDPSocketFactory(this);
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
		udpController.setPacketSender((PacketSender) sender);
	}

	@Override
	public void register(LowerSocketAdapter<UDPDatagram> adapter) {
		adapter.setSocketFactory(socketFactory);		
	}

	public UDPSocketFactory getSocketFactory() {
		return socketFactory;
	}
	
	

}
