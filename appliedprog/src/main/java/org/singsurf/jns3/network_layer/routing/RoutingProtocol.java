package org.singsurf.jns3.network_layer.routing;

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
import org.singsurf.jns3.hardware.DeviceFactory;
import org.singsurf.jns3.hardware.PrefixDevice;
import org.singsurf.jns3.hardware.RoutingDevice;
import org.singsurf.jns3.network_layer.EthernetPacketSender;
import org.singsurf.jns3.network_layer.IPAddress;
import org.singsurf.jns3.network_layer.IPPacket;
import org.singsurf.jns3.network_layer.IPPrefix;
import org.singsurf.jns3.network_layer.PacketSender;

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
	RoutingDevice device;
	
	NATTable natTable = new NATTable();
	Map<Integer, MacAndPrefix> prefixMap = new HashMap<>();
	byte seqNumber = 0;

	RoutingHandler routingHandler;
	
	record MacAndPrefix(MACAddress mac, IPPrefix prefix) {}
	
	public RoutingProtocol(RoutingDevice device) {
		this.name = device.getName() + "-NAT";
		this.device = device;
		routingHandler = new RoutingHandler(this);
	}


	public IPAddress allocatePublicIP(IPAddress iaddr) {
		if(seqNumber == 255) {
			Jns3Logger.logError(name,"No more available addresses");
			return null;
		}
		var preIP = device.getPrefix().getPrefix();
		var bytes = preIP.getBytes();
		bytes[3] = seqNumber++;
		var publicIP = new IPAddress(bytes);
		natTable.put(iaddr, publicIP);
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
		var eth = device.getEthernetProtocol();
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
		this.routingHandler.packetSender = (PacketSender) sender;
	}


	public MACAddress lookup(IPAddress addr) {
		for(var entry: prefixMap.entrySet()) {
			var mnp = entry.getValue();
			if(mnp.prefix.matches(addr)) {
				return mnp.mac;
			}
		}
		return null;
	}

	public IPAddress allocateLocalIP() {
		return DeviceFactory.getInstance().createSequentialLocalIPAddress();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		sb.append("NAT Table:\n");
		for(var entry: natTable.natMap.entrySet()) {
			sb.append("\t").append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
		}
		sb.append("Prefix Map:\n");
		for(var entry: prefixMap.entrySet()) {
			var mnp = entry.getValue();
			sb.append("\t").append(entry.getKey()).append("\t").append(mnp.mac).append("\t").append(mnp.prefix).append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
}
