package org.singsurf.jns3.network_layer;

import java.util.List;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameSender;
import org.singsurf.jns3.data_link_layer.MACAddress;
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
	public static final IPAddress LIMITED_MULTICAST_ADDRESS = new IPAddress("255.255.255.255");
	public static final short ETHERTYPE = 0x800;
	String name;
	ProtocolDevice device;
	boolean isRouter;
	
	IPFragmentHandler fragmentHandler;
	TTLDecrementor ttlDecrementor;
	ProtocolDelegator<IPPacket> delegator;
	EthernetPacketSender packetSender;
	ARPUpdater arpUpdater;
	IPFrameHandler frameHandler;
	DefaultPacketHandler defaultReciver;

	ARPProtocol arpProtocol;

	public IPProtocol(ProtocolDevice device, boolean isRouter) {
		this.device = device;
		this.isRouter = isRouter;
		this.name = device.getName() + "-IP";
		this.arpProtocol = device.getArpProtocol();
		
        packetSender = new EthernetPacketSender(device,this);
		
		this.arpUpdater = new ARPUpdater(this);
		this.delegator = new ProtocolDelegator<IPPacket>(device.getName());
		this.fragmentHandler = new IPFragmentHandler(device, delegator);
		if(isRouter) {
			this.ttlDecrementor = new TTLDecrementor(this, delegator);
			this.frameHandler = new IPFrameHandler(ttlDecrementor);
			delegator.addHandler(new IPPacketForwarder(this));
		} else {
			this.frameHandler = new IPFrameHandler(delegator);
			delegator.addHandler(fragmentHandler);
		}
	}
	
	// Port and adapter methods

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
		return List.of(arpUpdater,frameHandler);
	}

	@Override
	public void setInboundCallback(ProtocolReceiver<EthernetFrame> receiver) {
		
	}

	@Override
	public void setOutboundSender(ProtocolSender<EthernetFrame> sender) {
		var frameSender = (FrameSender) sender;
		
        packetSender.setFrameSender(frameSender);

	}

	public void addHandler(PacketHandler handler) {
		delegator.addHandler(handler);			
	}

	public void finalise() {
		defaultReciver = new DefaultPacketHandler(device.getName());
		delegator.addHandler(defaultReciver);
	}

/* Example output from ipconfig on Windows 11
Wireless LAN adapter WiFi:

   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : fe80::3168:74aa:4574:86f7%2
   IPv4 Address. . . . . . . . . . . : 10.43.168.48
   Subnet Mask . . . . . . . . . . . : 255.255.0.0
   Default Gateway . . . . . . . . . : 10.43.0.1
12345678901234567890123456789012345678901234567890
         1         2         3         4      
 */
	public void displayIPConfig() {
		var terminal = device.getTerminal();
		terminal.display("Ethernet adapter "+ name +":\n");
		displayIPConfigLine("IPv4 Address", device.getIPAddress().toString());
		displayIPConfigLine("Subnet Mask", device.getSubnetMask().toString());
		displayIPConfigLine("Default Gateway", device.getDefaultGateway().toString());
	}
	
	private void displayIPConfigLine(String name, String value) {
		var terminal = device.getTerminal();
		var sb = new StringBuilder();
		sb.append("   ").append(name);
		if(sb.length() % 2 ==0) {
			sb.append(" ");
		}
		while(sb.length() < 37) {
			sb.append(". ");
		}
		sb.append(": ").append(value);
		terminal.display(sb.toString());
	}
	
	// Getters
	
	public ProtocolDelegator<IPPacket> getDelegator() {
		return delegator;
	}

	public PacketSender getPacketSender() {
		return packetSender;
	}

	public IPFragmentHandler getFragmentHandler() {
		return fragmentHandler;
	}

	public DefaultPacketHandler getDefaultReceiver() {
		return defaultReciver;
	}

	public boolean isRouter() {
		return isRouter;
	}

	protected void sendArpRequest(IPAddress destIP) {
		arpProtocol.sendArpRequest(destIP);		
	}

	protected MACAddress lookup(IPAddress destIP) {
		if(device.getSubnetMask().matches(device.getIPAddress(), destIP))
			return arpProtocol.lookup(destIP);
		if(destIP.equals(IPProtocol.LIMITED_MULTICAST_ADDRESS) ||
				destIP.equals(IPAddress.ZERO_ADDRESS) ||
				destIP.equals(device.getMulticastAddress())) {
			return arpProtocol.lookup(destIP);
		}
		var gate = device.getDefaultGateway();
		var mac = arpProtocol.lookup(gate);
		if(mac != null) {
			return mac;
		}
		var rp = device.getRoutingProtocol();
		if(rp != null) {
			mac = rp.lookup(destIP);
			if(mac != null) {
				return mac;
			}
		}
		
//		Jns3Logger.logError(name, "IP address " + destIP + " is not on the same subnet as " + device.getIPAddress() + " with subnet mask " + device.getSubnetMask());
		return null;
	}

}
