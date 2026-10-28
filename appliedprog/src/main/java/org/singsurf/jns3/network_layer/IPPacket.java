package org.singsurf.jns3.network_layer;

import java.util.Arrays;

import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.generic_protocol.ProtocolContainer;

/**
 * Represents an IP packet with header and data.
 * <p>
 * The header consist of
 * <ul>
 * <li> `version` 4 bits with the value 0x4 for IPv4
 * <li> `headerLength` 4 bits with value 0x5 when the options are omitted.
 * <li> `typeOfService` 8 bits, 1 byte, unused here with value 0x00
 * <li> `totalLength` 16 bit, 2 bytes, length of header and data sections.
 * <li> `identification` 16 bit, 2 bytes, used for uniquely identifying the group of fragments of a single IP datagram.
 * <li> `flagReserved` 1 bit, 0.
 * <li> `flagDF` 1 bit, don't fragment. 0
 * <li> `flagMF` 1 bit, whether there are more fragments, 1 for more 0 for last fragment.
 * <li> `fragmentOffset` 13 bits, 0 for the first fragment or an un-fragmented datagram, otherwise the offset in 8-byte blocks of the fragment.
 * <li> `timeToLive` 8 bit, 1 byte. Number of hops before the packet is dropped.
 * <li> `protocol` 8 bit, 1 byte. 0x01 for IMCP, 0x06 for TCP, 17, 0x11 for UDP.
 * <li> `headerChecksum` 16-bit 2 byte. Calculated by taking the 1's complement of the 1's complement sum of the 5 16 bit words in the header.
 * <li> `sourceAddress` 4 byte, source IP address
 * <li> `destinationAddress` 4 byte, destination IP address
 * <li> `options` not used 0 bytes.
 * </ul>
 * 
 */
public class IPPacket implements ProtocolContainer {
    public static final byte IMCP = 0x01;
    public static final byte TCP = 0x06;
    public static final byte UDP = 0x11;
	public static final int IPV4_ID = 0x45;

    byte version = 0x4; // 4 bits
    byte headerLength = 0x5; // 4 bits
    byte typeOfService = 0x00; // unused here
    short totalLength; // calculates automatically based on header and data length
    short identification=0x0000; // used for fragments
    byte flags; // 3 bits for flags (e.g., DF, MF)
    short fragmentOffset; // 13 bits for fragment offset
    byte timeToLive;
    byte protocol;
    short headerChecksum;
    IPAddress sourceAddress;
    IPAddress destinationAddress;
    byte[] data;

    /**
     * Represents information about a fragment of a large fragment.
     */
    public record FragmentInfo(short identification, boolean dontFragment, boolean moreFragments, short fragmentOffset) {
        public FragmentInfo() { 
            this((short) 0, true, false, (short) 0);
        }
        public FragmentInfo(int identification, boolean dontFragment, boolean moreFragments, int fragmentOffset) {
            this((short) identification, dontFragment, moreFragments, (short) fragmentOffset);
        }

        public FragmentInfo(short identification, short flagsAndFragmentOffset) {
            this(
                (short) identification, // identification
                (flagsAndFragmentOffset & 0x02) != 0, // dontFragment
                (flagsAndFragmentOffset & 0x04) != 0, // moreFragments
                (short) ((flagsAndFragmentOffset >> 3) & 0x1FFF) // fragmentOffset
            );
        }
    }

        //     return new IPPacket(
        //     versionAndHeaderLength, typeOfService, totalLength, 
        //     flagsAndFragmentOffset,
        //     timeToLive, protocol, headerChecksum,
        //     sourceAddress, destinationAddress, data
        // );

    private IPPacket(
        byte versionAndHeaderLength, byte typeOfService, short totalLength, 
        FragmentInfo flagsAndFragmentOffset, 
        byte timeToLive, byte protocol, short headerChecksum, 
        IPAddress sourceAddress, IPAddress destinationAddress, byte[] data) {
        this.version = (byte) ((versionAndHeaderLength >> 4) & 0x0F);
        this.headerLength = (byte) (versionAndHeaderLength & 0x0F);
        this.typeOfService = typeOfService;
        this.totalLength = totalLength;
        this.flags = (byte) ((flagsAndFragmentOffset.dontFragment() ? 0x02 : 0x00) | (flagsAndFragmentOffset.moreFragments() ? 0x04 : 0x00));
        this.fragmentOffset = flagsAndFragmentOffset.fragmentOffset();
        this.identification = flagsAndFragmentOffset.identification();
        this.timeToLive = timeToLive;
        this.protocol = protocol;
        this.headerChecksum = headerChecksum;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.data = data;
    }

    public IPPacket(byte protocol, FragmentInfo frag,
            byte timeToLive, 
            IPAddress sourceAddress, IPAddress destinationAddress, byte[] data) {
        this.data = data != null ? data.clone() : new byte[0];
        this.totalLength = (short) (20 + data.length);
        this.flags = 0;
        if (frag.dontFragment()) this.flags |= 0x02;
        if (frag.moreFragments()) this.flags |= 0x04;
        this.fragmentOffset = frag.fragmentOffset();
        this.identification = frag.identification();    
        this.timeToLive = timeToLive;
        this.protocol = protocol;
        this.headerChecksum = 0x0000;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.headerChecksum = calculateHeaderChecksum().getComplement().getValue();
    }

    public IPPacket(int protocol,
        int timeToLive, 
        IPAddress sourceAddress, IPAddress destinationAddress, byte[] data) {
        this((byte)protocol, new FragmentInfo(), (byte) timeToLive, sourceAddress, destinationAddress, data);
    }

    public byte getVersion() {
        return version;
    }

    public byte getHeaderLength() {
        return headerLength;
    }

    public byte getVersionAndHeaderLength() {
        return (byte) ((version << 4) | headerLength);
    }

    public byte getTypeOfService() {
        return typeOfService;
    }
    public short getTotalLength() {
        return totalLength;
    }
    public short getIdentification() {
        return identification;
    }
    public boolean isDontFragment() {
        return (flags & 0x02) != 0;
    }

    public boolean isMoreFragments() {
        return (flags & 0x04) != 0;
    }  

    public short getFragmentOffset() {
        return fragmentOffset;
    }

    public short getFlagsAndFragmentOffset() {
        return (short) (((flags & 0x07)) | (fragmentOffset << 3 & 0x1FFF));
    }

    public short getChecksum() {
        return headerChecksum;
    }

    public IPAddress getSourceAddress() {
        return sourceAddress;
    }

    public IPAddress getDestinationAddress() {
        return destinationAddress;
    }
    
    public byte[] getContents() {
        return data.clone();
    }

    public OnesComplementNumber calculateHeaderChecksum() {
        var sum = new OnesComplementNumber(
            getVersionAndHeaderLength(),
            typeOfService
        );

        sum = sum.add(totalLength);
        sum = sum.add(identification);
        sum = sum.add(getFlagsAndFragmentOffset());
        sum = sum.add(timeToLive,protocol); 
        sum = sum.add(sourceAddress.getHighWord());
        sum = sum.add(sourceAddress.getLowWord());
        sum = sum.add(destinationAddress.getHighWord());
        sum = sum.add(destinationAddress.getLowWord());
        sum = sum.add(headerChecksum);

        return sum;
    }
    
    public boolean isValid() {
		return calculateHeaderChecksum().equals(OnesComplementNumber.MINUS_ZERO);
	}

    public byte[] getBytes() {
        byte[] bytes = new byte[totalLength];
        bytes[0] = getVersionAndHeaderLength();
        bytes[1] = typeOfService;
        bytes[2] = (byte) (totalLength >> 8);
        bytes[3] = (byte) (totalLength & 0xFF);
        bytes[4] = (byte) (identification >> 8);
        bytes[5] = (byte) (identification & 0xFF);
        bytes[6] = (byte) (getFlagsAndFragmentOffset() >> 8);
        bytes[7] = (byte) (getFlagsAndFragmentOffset() & 0xFF);
        bytes[8] = timeToLive;
        bytes[9] = protocol;
        bytes[10] = (byte) (headerChecksum >> 8);
        bytes[11] = (byte) (headerChecksum & 0xFF);
        System.arraycopy(sourceAddress.getBytes(), 0, bytes, 12, 4);
        System.arraycopy(destinationAddress.getBytes(), 0, bytes, 16, 4);
        System.arraycopy(data, 0, bytes, 20, data.length);
        return bytes;
    }
    
    static boolean isIPPacket(byte[] bytes) {
		if (bytes.length < 20) {
			return false;
		}
		byte versionAndHeaderLength = bytes[0];
		byte version = (byte) (((byte) versionAndHeaderLength >> 4) & 0x0F);
		return version == 4;
	}

    private static IPPacket fromBytes(byte[] bytes) {
        byte versionAndHeaderLength = bytes[0];
        byte typeOfService = bytes[1];
        short totalLength = (short) ((bytes[2] << 8) | (bytes[3] & 0xFF));
        short identification = (short) ((bytes[4] << 8) | (bytes[5] & 0xFF));
        short flagsAndFragmentOffset = (short) ((bytes[6] << 8) | (bytes[7] & 0xFF));
        var fragmentInfo = new FragmentInfo(identification, flagsAndFragmentOffset);
        byte timeToLive = bytes[8];
        byte protocol = bytes[9];
        short headerChecksum = (short) ((bytes[10] << 8) | (bytes[11] & 0xFF));
        IPAddress sourceAddress = new IPAddress(Arrays.copyOfRange(bytes, 12, 16));
        IPAddress destinationAddress = new IPAddress(Arrays.copyOfRange(bytes, 16, 20));
        byte[] data = Arrays.copyOfRange(bytes, 20, bytes.length);

        return new IPPacket(
            versionAndHeaderLength, typeOfService, totalLength, fragmentInfo,
            timeToLive, protocol, headerChecksum,
            sourceAddress, destinationAddress, data
        );
    }

	public IPPacket createFragment(int offset, int dataSize, boolean moreFrag, int id) {
	    var frag = new FragmentInfo((short) id, false, moreFrag, (short) offset);
	    byte[] dataSection = Arrays.copyOfRange(data, 8*offset, 8*offset + dataSize);
	    return new IPPacket(protocol, frag, timeToLive, sourceAddress, destinationAddress, dataSection);
	}

	public int getDataLength() {
		return data.length;
	}

	public byte getProtocol() {
		return protocol;
	}

	public byte getTimeToLive() {
		return timeToLive;
	}

	public String getDataAsString() {
		return new String(data);
	}

	public void decrementTTL() {
		--timeToLive;
		recalculateHeaderChecksum();
	}
	
	@Override
	public String toString() {
		return String.format("IPPacket[ver=%d,hlen=%d,tos=%d,len=%d,id=%d,flags=0x%02X,offset=%d,ttl=%d,proto=%d,checksum=0x%04X,src=%s,dst=%s,dataLen=%d]",
				version, headerLength, typeOfService, totalLength,
				identification, flags, fragmentOffset,
				timeToLive, protocol, headerChecksum & 0xFFFF,
				sourceAddress.toString(), destinationAddress.toString(),
				data.length);
	}

	public void setTimeToLive(int i) {
		timeToLive = (byte) i;
		recalculateHeaderChecksum();
	}

	private void recalculateHeaderChecksum() {
		headerChecksum = 0;
		headerChecksum = this.calculateHeaderChecksum().getComplement().getValue();
	}

	static IPPacket fromFrame(EthernetFrame frame) {
		return IPPacket.fromBytes(frame.getContents());
	}

}
