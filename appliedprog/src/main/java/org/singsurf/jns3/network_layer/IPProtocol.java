package org.singsurf.jns3.network_layer;

import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.generic_protocol.UpperPort;
import org.singsurf.jns3.hardware.AddressableDevice;

public class IPProtocol implements LowerAdapter<EthernetFrame>, UpperPort<IPPacket> {
	AddressableDevice device;
	IPFragmentHandler fragmentHandler;
	IPDelegator delegator;
	EthernetPacketSender packetSender;
	FrameSender frameSender;
	ARPProtocol arpProtocol;
	
	public IPProtocol(AddressableDevice device, ARPProtocol arpProtocol) {
		this.device = device;
		this.arpProtocol = arpProtocol;
		this.delegator = new IPDelegator(device.getName());
		this.fragmentHandler = new IPFragmentHandler(device, delegator);
//        		
//        		frameSender);

	}

	@Override
	public void register(LowerAdapter<IPPacket> adapter) {
		for(var h: adapter.getInboundHandlers()) {
			delegator.addHandler(h);
		}
		adapter.setOutboundSender(packetSender);
		adapter.setInboundCallback(delegator);
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
		frameSender = (FrameSender) sender;
		
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
}
