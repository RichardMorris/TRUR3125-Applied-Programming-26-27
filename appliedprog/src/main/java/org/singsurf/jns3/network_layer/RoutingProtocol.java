package org.singsurf.jns3.network_layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.LowerAdapter;
import org.singsurf.jns3.generic_protocol.Protocol;
import org.singsurf.jns3.generic_protocol.ProtocolHandler;
import org.singsurf.jns3.generic_protocol.ProtocolReceiver;
import org.singsurf.jns3.generic_protocol.ProtocolSender;
import org.singsurf.jns3.hardware.PrefixDevice;
import org.singsurf.jns3.hardware.ProtocolDevice;

/**
 * A routing protocol that translates between private and public IP addresses.
 * It maintains a NAT table mapping private to public IP's.
 * LAN devices are assigned public IP addresses.
 * and a mapping of prefixes to MAC addresses.
 * The {@link RoutingHandler} is used to handle incoming packets and translate their destination IP addresses.
 * Outgoing packets are sent via the {@link EthernetPacketSender} 
 * that uses this protocol to translate the source IP.
 */
public class RoutingProtocol implements Protocol, LowerAdapter<IPPacket> {
	String name;
	IPPrefix publicPrefix;
	
	NATTable natTable = new NATTable();
	List<IPAddress> allocatedPublicIPs = new ArrayList<>();
	byte seqNumber = 0;
	Map<Integer, MacAndPrefix> prefixMap = new HashMap<>();
	IPAddress publicIP;
	RoutingHandler routingHandler;
	PrefixDevice prefixDevice;
	ProtocolDevice protocolDevice;
	PacketSender packetSender;
	
	record MacAndPrefix(MACAddress mac, IPPrefix prefix) {}
	
	public RoutingProtocol(ProtocolDevice device) {
		this.name = device.getName() + "NAT";
		this.prefixDevice = (PrefixDevice) device;
		this.protocolDevice = device;
		this.publicPrefix = prefixDevice.getPrefix();
		this.publicIP = prefixDevice.getPublicIP() != null
				? prefixDevice.getPublicIP()
				: allocatePublicIP(device.getIPAddress());
		routingHandler = new RoutingHandler(this);
	}


	public IPAddress allocatePublicIP(IPAddress iaddr) {
		if(seqNumber == 255) {
			Jns3Logger.logError(name,"No more available addresses");
			return null;
		}
		var preIP = publicPrefix.getPrefix();
		var bytes = preIP.getBytes();
		bytes[3] = seqNumber++;
		var publicIP = new IPAddress(bytes);
		natTable.put(iaddr, publicIP);
		allocatedPublicIPs.add(publicIP);
		return publicIP;
	}


	public IPAddress getPublicIP() {
		return publicIP;
	}


	public IPAddress getPublicIP(IPAddress ipAddress) {
		return natTable.getPublicIP(ipAddress);
	}
	
	public IPAddress getPrivateIP(IPAddress publicIP) {
		return natTable.getPrivateIP(publicIP);
	}


	public void registerDevice(int i, PrefixDevice dev) {
		prefixMap.put(i, new MacAndPrefix(dev.getMACAddress(), dev.getPrefix()));		
		var eth = protocolDevice.getEthernetProtocol();
		eth.putPortMapping(i, dev.getMACAddress());
	}


	@Override
	public List<ProtocolHandler<IPPacket>> getInboundHandlers() {
		return List.of(routingHandler);
	}


	@Override
	public void setInboundCallback(ProtocolReceiver<IPPacket> receiver) {
	}


	@Override
	public void setOutboundSender(ProtocolSender<IPPacket> sender) {
		this.packetSender = (PacketSender) sender;
	}


	public MACAddress lookup(IPAddress addr) {
		for(var entry: prefixMap.entrySet()) {
			var mnp = entry.getValue();
			if(mnp.prefix.matches(addr)) {
				return mnp.mac;
			}
		}
		if(prefixDevice.getPrefix().matches(addr)) {
			return prefixDevice.getMACAddress();
		}
		return null;
	}


	public PacketSender getPacketSender() {
		return packetSender;
	}

}
