package org.singsurf.jns3.network_layer;

public class IPPacket {
    public static final byte IMCP = 0x01;
    public static final byte TCP = 0x06;
    public static final byte UDP = 0x11;

    final byte version = 0x4; // 4 bits
    final byte headerLength = 0x5; // 4 bits
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

    public record FragmentInfo(short identification, boolean dontFragment, boolean moreFragments, short fragmentOffset) {
        public FragmentInfo() { 
            this((short) 0, true, false, (short) 0);
        }
        public FragmentInfo(int identification, boolean dontFragment, boolean moreFragments, int fragmentOffset) {
            this((short) identification, dontFragment, moreFragments, (short) fragmentOffset);
        }
    }

    public IPPacket(int protocol,
        int timeToLive, 
        IPAddress sourceAddress, IPAddress destinationAddress, byte[] data) {
        this((byte)protocol, new FragmentInfo(), (byte) timeToLive, sourceAddress, destinationAddress, data);
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
    public boolean getDontFragment() {
        return (flags & 0x02) != 0;
    }

    public boolean getMoreFragments() {
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
    
    public byte[] getData() {
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
}
