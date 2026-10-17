package org.singsurf.jns3.network_layer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.data_link_layer.FrameHandler;
import org.singsurf.jns3.hardware.PC;

public class FragmentCombiner implements FrameHandler {
	PC pc;
	PacketReceiver receiver;
	Map<IPAddress, Map<Integer, FragmentCollector>> collectorsBySource = new HashMap<>();

	
	public FragmentCombiner(PC pc, PacketReceiver receiver) {
		this.pc = pc;
		this.receiver = receiver;
	}
	
	class FragmentCollector {
		short identification;
		Map<Integer,IPPacket> fragments=new TreeMap<>();
		private byte protocol;
		private IPAddress sourceAddress;
		private IPAddress destinationAddress;
		private byte ttl;
		
		public FragmentCollector(short identification) {
			this.identification = identification;
		}
		
		boolean isComplete() {
			int offset = 0;
			IPPacket fragment=null;
			for(var ent : fragments.entrySet()) {
				if(ent.getKey() != offset) {
					return false;
				}
				fragment = ent.getValue();
				offset += fragment.getDataLength() / 8;
			}
			if(!fragment.isMoreFragments()) {
				return true;
			}
			return false;
		}

		boolean matches(IPPacket fragment) {
			if(fragments.isEmpty()) {
				protocol = fragment.getProtocol();
				sourceAddress = fragment.getSourceAddress();
				destinationAddress = fragment.getDestinationAddress();
				ttl = fragment.getTimeToLive();
			} else {
				if(protocol != fragment.getProtocol()) {
					Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Fragment protocol mismatch: " + protocol + " vs " + fragment.getProtocol());
					return false;
				}
				if(!sourceAddress.equals(fragment.getSourceAddress())) {
					Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Fragment source address mismatch: " + sourceAddress + " vs " + fragment.getSourceAddress());
					return false;
				}
				if(!destinationAddress.equals(fragment.getDestinationAddress())) {
					Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Fragment destination address mismatch: " + pc.getIPAddress() + " vs " + fragment.getDestinationAddress());
					return false;
				}
				if(identification != fragment.getIdentification()) {
					Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Fragment identification mismatch: " + identification + " vs " + fragment.getIdentification());
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Adds a fragment to the collector. If the collector is complete after adding the fragment, it combines the data sections and sends the complete packet to the receiver.
		 * @param fragment
		 * @return true if the fragment was added and the collector is complete, false otherwise
		 */
		public boolean addFragment(IPPacket fragment) {
			if(!matches(fragment)) {
				return false;
			}
			fragments.put((int)fragment.getFragmentOffset(), fragment);

			if(isComplete()) {
				byte[] data = combineDataSections();
				var completePacket = new IPPacket(protocol, ttl,
						sourceAddress, destinationAddress, data);
				receiver.receivePacket(completePacket);
				return true;
			} else {
				return false;
			}			
		}

		protected byte[] combineDataSections() {
			int totalLength = 0;
			for(var frag : fragments.values()) {
				totalLength += frag.getDataLength();
			}
			byte[] data = new byte[totalLength];
			int pos = 0;
			for(var frag : fragments.values()) {
				System.arraycopy(frag.getData(), 0, data, pos, frag.getDataLength());
				pos += frag.getDataLength();
			}
			return data;
		}
	}
	

	@Override
	public boolean handleFrame(EthernetFrame frame) {
		var payload = frame.getPayload();
		if(payload.length == 0) {
			return false;
		}
		if(payload[0]  != IPPacket.IPV4_ID) {
			return false;
		}
		if(payload.length < 20) {
			Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "IP packet too short: " + payload.length);
			return true;
		}
		IPPacket packet;
		try {
			packet = IPPacket.fromBytes(payload);
		}
		catch(Exception e) {
			Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Error parsing IP packet: " + e.getMessage());
			return true;
		}
		return handlePacket(packet);
	}

	protected boolean handlePacket(IPPacket packet) {
		if(!packet.isValidChecksum()) {
			Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Invalid IP packet checksum");
			return true;
		}
		if(!packet.getDestinationAddress().equals(pc.getIPAddress())) {
			Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "IP packet not for this PC: " + packet.getDestinationAddress());
			return true;
		}
		if(packet.getIdentification() ==0) {
			if(packet.isMoreFragments()) {
				Jns3Logger.log(pc.getName(),Jns3Logger.LogLevel.ERROR, "Invalid fragment info for packet with identification 0");
				return true;
			}
			else {
				receiver.receivePacket(packet);
				return true;
			}
		}
		var source = packet.getSourceAddress();
		var identification = packet.getIdentification();
		var sourceCollectors = collectorsBySource.computeIfAbsent(source, k -> new HashMap<>());
		var collector = sourceCollectors.computeIfAbsent((int) identification, k -> new FragmentCollector(identification));
		boolean complete = collector.addFragment(packet);
		if(complete) {
			sourceCollectors.remove((int) identification);
			if(sourceCollectors.isEmpty()) {
				collectorsBySource.remove(source);
			}
		}
		return true;
	}

}
