package org.singsurf.jns3.network_layer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.generic_protocol.HandlerStatus;
import org.singsurf.jns3.generic_protocol.ProtocolDelegator;
import org.singsurf.jns3.hardware.AddressableDevice;

/**
 * Handles incoming fragmented IP packets.
 * Packets are held by the inner FragmentCollector
 * until all fragments have been received,
 * and then passed down the receiver chain as a complete packet.
 * <p>
 * See <a href="../../../../../../../Week11.md">Week 11</a> for the logic
 * behind the fragment reassembly.
 */
public class IPFragmentHandler implements PacketHandler {
	AddressableDevice pc;
	ProtocolDelegator<IPPacket> receiver;
	Map<IPAddress, Map<Integer, FragmentCollector>> collectorsBySource = new HashMap<>();

	
	public IPFragmentHandler(AddressableDevice pc, ProtocolDelegator<IPPacket> receiver) {
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
					Jns3Logger.logError(pc.getName(), "Fragment protocol mismatch: " + protocol + " vs " + fragment.getProtocol());
					return false;
				}
				if(!sourceAddress.equals(fragment.getSourceAddress())) {
					Jns3Logger.logError(pc.getName(), "Fragment source address mismatch: " + sourceAddress + " vs " + fragment.getSourceAddress());
					return false;
				}
				if(!destinationAddress.equals(fragment.getDestinationAddress())) {
					Jns3Logger.logError(pc.getName(), "Fragment destination address mismatch: " + pc.getIPAddress() + " vs " + fragment.getDestinationAddress());
					return false;
				}
				if(identification != fragment.getIdentification()) {
					Jns3Logger.logError(pc.getName(), "Fragment identification mismatch: " + identification + " vs " + fragment.getIdentification());
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
				receiver.receive(completePacket);
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
				System.arraycopy(frag.getContents(), 0, data, pos, frag.getDataLength());
				pos += frag.getDataLength();
			}
			return data;
		}
	}
	
	@Override
	public HandlerStatus handle(IPPacket packet) {
		if(packet.getIdentification() ==0) {
			if(packet.isMoreFragments()) {
				return HandlerStatus.invalid("Invalid fragment info for packet with identification 0");
			}
			else {
				return HandlerStatus.CONTINUE;
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
		return HandlerStatus.COMPLETED;
	}

}
