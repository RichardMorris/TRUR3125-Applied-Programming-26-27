package org.singsurf.jns3.network_layer;

import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.ProtocolDevice;

public class IPProtocol implements Protocol, LowerAdapter<EthernetFrame>, UpperPort<IPPacket> {
	ProtocolDevice device;
	IPFragmentHandler fragmentHandler;
	IPDelegator delegator;
	EthernetPacketSender packetSender;
	ARPProtocol arpProtocol;
	public static final IPAddress LIMITED_MULTICAST_ADDRESS = new IPAddress("255.255.255.255");
	
	public IPProtocol(ProtocolDevice device) {
		this.device = device;
		this.arpProtocol = device.getArpProtocol();
		this.delegator = new IPDelegator(device.getName());
		this.fragmentHandler = new IPFragmentHandler(device, delegator);
	}

	@Override
	public void register(LowerAdapter<IPPacket> adapter) {
		adapter.setOutboundSender(packetSender);
		adapter.setInboundCallback(delegator);
		for(var h: adapter.getInboundHandlers()) {
			delegator.addHandler(h);
		}
	}

	@Override
	public List<ProtocolHandler<EthernetFrame>> getInboundHandlers() {
		return List.of(fragmentHandler);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<EthernetFrame> receiver) {
		
	}

	@Override
	public void setOutboundSender(ProtocolSender<EthernetFrame> sender) {
		var frameSender = (FrameSender) sender;
		
        packetSender = new EthernetPacketSender(device, 
        		arpProtocol.getArpController(),frameSender);

	}

	public void addHandler(PacketHandler handler) {
		delegator.addHandler(handler);			
	}

	public void finalise() {
		delegator.addHandler(new DefaultPacketReceiver(device.getName()));
	}

	public ProtocolDelegator<IPPacket> getDelegator() {
		return delegator;
	}

	public PacketSender getPacketSender() {
		return packetSender;
	}

	public void receive(IPPacket packet) {
		delegator.receive(packet);		
	}
}
