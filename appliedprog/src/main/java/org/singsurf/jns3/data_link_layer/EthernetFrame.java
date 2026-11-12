package org.singsurf.jns3.data_link_layer;

import java.util.Arrays;
import java.util.zip.CRC32;

import org.singsurf.jns3.data_link_layer.arp.ARPProtocol;
import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPProtocol;

/**
 * Represents an Ethernet frame:
 * <ul>
    * <li>6 byte destination mac address
    * <li>6 byte source mac address
    * <li>a 2 byte payload length &lt;= 1500 (value &gt; 1536 are special EtherType values)
    * <li>the payload
    * <li>4 byte frame check sum, CRC32
    * </ul>
 * See <a href="../../../../../../Week4.md">Week 4</a> for details on the Ethernet frame format.
 */

public class EthernetFrame implements ProtocolContainer {
    MACAddress destination;
    MACAddress source;
    short etherType;
    byte[] payload;
    int frameCheck;

    public EthernetFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload) {
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.payload = payload;
        this.frameCheck = calcFrameCheck();
        if(etherType == ARPProtocol.ARP_ETHER_TYPE
        		|| etherType == IPProtocol.ETHERTYPE) { 
            return; // Skip further checks for ARP frames
        }
        if(etherType <= 0 || etherType > 1500) {
            throw new IllegalArgumentException("Invalid etherType: " + etherType);
        }
        if(payload == null || payload.length != etherType) {
            throw new IllegalArgumentException("Invalid payload length: " + (payload == null ? 0 : payload.length));
        }
    }

    static EthernetFrame parseBytes(byte[] frame) {
        if(frame.length < 14 + 4) {
            throw new IllegalArgumentException("Frame too short");
        }
        byte[] destBytes = new byte[6];
        byte[] srcBytes = new byte[6];
        System.arraycopy(frame, 0, destBytes, 0, 6);
        System.arraycopy(frame, 6, srcBytes, 0, 6);
        MACAddress destination = new MACAddress(destBytes);
        MACAddress source = new MACAddress(srcBytes);
        short etherType = (short)((frame[12] << 8) | (frame[13] & 0xFF));
        byte[] payload = new byte[frame.length - 14 - 4];
        System.arraycopy(frame, 14, payload, 0, payload.length);
        int crc = 
                (frame[14 + payload.length] & 0xFF) << 24 |
        		(frame[14 + payload.length + 1] & 0xFF) << 16 |
        		(frame[14 + payload.length + 2] & 0xFF) << 8 |
        		(frame[14 + payload.length+ 3] & 0xFF) ;
        return new EthernetFrame(destination, source, etherType, payload, crc);
    }

    private EthernetFrame(
        MACAddress destination, 
        MACAddress source, 
        short etherType, 
        byte[] payload, int frameCheck) {
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.payload = payload;
        this.frameCheck = frameCheck;
    }

    public EthernetFrame(MACAddress destMac, MACAddress sourceMac, byte[] bytes) {
		this(destMac, sourceMac, (short) bytes.length, bytes);
	}

	public MACAddress getDestination() {
        return destination;
    }

    public MACAddress getSource() {
        return source;
    }

    public short getEtherType() {
        return etherType;
    }

	@Override
    public byte[] getContents() {
        return payload;
    }

	@Override
    public byte[] getBytes() {
        byte[] frame = new byte[14 + payload.length + 4]; // 14 bytes for header, 4 bytes for CRC
        System.arraycopy(destination.getBytes(), 0, frame, 0, 6);
        System.arraycopy(source.getBytes(), 0, frame, 6, 6);
        frame[12] = (byte)(etherType >> 8);
        frame[13] = (byte)(etherType & 0xFF);
        System.arraycopy(payload, 0, frame, 14, payload.length);
        int crc = getFrameCheck();
        frame[14 + payload.length]     = (byte)(crc >> 24);
        frame[14 + payload.length + 1] = (byte)(crc >> 16);
        frame[14 + payload.length + 2] = (byte)(crc >> 8);
        frame[14 + payload.length + 3] = (byte)(crc & 0xFF);
        return frame;
    }
    public String getPayloadAsString() {
        return new String(payload);
    }
    
    public int calcFrameCheck() {
        CRC32 crc = new CRC32();
        crc.update(destination.getBytes());
        crc.update(source.getBytes());
        crc.update((byte)(etherType >> 8));
        crc.update((byte)(etherType & 0xFF));
        crc.update(payload);
        return (int) crc.getValue();
    }
    public int getFrameCheck() {
        return frameCheck;
    }
    public boolean isValid() {
		return frameCheck == calcFrameCheck();
	}
    
    @Override
    public String toString() {
        return "EthernetFrame{" +
                "destination=" + destination +
                ", source=" + source +
                ", etherType=" + etherType +
                ", payload=" + new String(payload) +
                '}';
    }
    
    @Override
    public int hashCode() {
        int result = destination.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + Short.hashCode(etherType);
        result = 31 * result + Arrays.hashCode(payload);
        result = 31 * result + Long.hashCode(frameCheck);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EthernetFrame that = (EthernetFrame) obj;
        return etherType == that.etherType &&
                frameCheck == that.frameCheck &&
                destination.equals(that.destination) &&
                source.equals(that.source) &&
                Arrays.equals(payload, that.payload);
    }


}
