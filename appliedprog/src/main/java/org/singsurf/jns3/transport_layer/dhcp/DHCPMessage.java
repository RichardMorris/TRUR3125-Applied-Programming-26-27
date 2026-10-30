package org.singsurf.jns3.transport_layer.dhcp;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.data_link_layer.MACAddress;
import org.singsurf.jns3.generic_protocol.ProtocolContainer;
import org.singsurf.jns3.network_layer.IPAddress;

/**
 * 
 * OP - 1 byte op type, 0x01 for discover
 * HTYPE - 1 byte hardware type, 0x01 for ethernet
 * HLEN - 1 byte header length - 0x06
 * HOPS - number of hops, 0x00
 * XID - 4 bytes, 0x3903F326
 * SECS - 2 byte, 0x0000
 * FLAGS - 2 byte, 0x0000
 * CIADDR - Client IP address, 4 byte
 * YIADDR  - Your IP address, 4 bytes
 * SIADDR - Server IP address, 4 bytes
 * GIADDR - Gateway IP address: 4 bytes
 * CHADDR -Client Hardware address, 4 * 4 bytes
 * BOOTP File Name Options - 192 octets all zero, not implemented
 * MAGIC COOKIE - marks end of options 4 byte 0x63825363

 */
public class DHCPMessage implements ProtocolContainer {

	public static final byte DHCP_OP_DISCOVER = 0x01;
	public static final byte DHCP_OP_OFFER = 0x02;
	public static final byte DHCP_OP_REQUEST = 0x03;
	public static final byte DHCP_OP_DECLINE = 0x04;
	public static final byte DHCP_OP_ACK = 0x05;

	public static final short DHCP_FLAG_UNICAST = 0x00;
	public static final short DHCP_FLAG_BROARDCAST = 0x01;

	public static final int DHCP_MAGIC_COOKIE = 0x63825363;
	public static final int DHCP_OPTION_END = 0xff;
	
	public static final byte DHCP_HEADER_SIZE = 44; // 240 in real protocol
	public static final byte DHCP_BOOTP_SIZE = 0;
	public static final byte DHCP_MAGIC_SIZE = 4; // for magic cookie
	
	byte op; // DHCP message type, octet 0
	byte htype=1; // Hardware type, 1 for ethernet, octet 1
	byte hlen=6; // six bytes for ethernet Mac address, octet 2
	byte hops=0; // number of hops, unused here, octet 3
	
	int xid; // unique transaction ID for the sequence of messages, octets 4-7
	
	short secs; // seconds elapsed since client started trying to get an IP address, number of tick used here, octets 8-9
	short flags; // 0x0000 for unicast, 0x0001 for broadcast, octets 10-11
	
	IPAddress ciaddr; // clients current IP address, 0x00000000 if client does not have an IP address yet, octets 12-15
	IPAddress yiaddr; // assigned IP address for the client, 0x00000000 if client does not have an IP address yet, octets 16-19
	IPAddress siaddr; // Server's IP address, octets 20-23
	IPAddress giaddr; // Gateway IP address, octets 24-27
	
	byte[] chaddr = new byte[16]; // client hardware address, in our case ethernet mac address, 6 bytes, rest is zero, octets 28-43
	// 192 bytes of boot file name, not implemented
	// byte[] bootFileName = new byte[192];
	
	int magicCookie = DHCP_MAGIC_COOKIE; // 4 bytes, octets 236-239 in real protocol, here octet 44-47
	List<DHCPOption> options = new ArrayList<>(); // variable length options field, starts with magic cookie 0x63825363, ends with 0xff
	
	public record DHCPOption(byte code, byte length, byte[] data) {
		public int getTotalLength() {
			return 2 + length; // 1 byte for code, 1 byte for length, and length bytes for data
		}
		public boolean isWellFormed() {
			return data.length == length;
		}
		public byte[] getBytes() {
			byte[] bytes = new byte[2 + length];
			bytes[0] = code;
			bytes[1] = length;
			System.arraycopy(data, 0, bytes, 2, length);
			return bytes;
		}
	}
	
	public record DHCPAddresses(MACAddress mac, IPAddress ciaddr, IPAddress yiaddr, IPAddress siaddr, IPAddress giaddr) {}
	
	public DHCPMessage(byte op, int xid, short secs, short flags, DHCPAddresses addresses,
			List<DHCPOption> options) {
		super();
		this.op = op;
		this.xid = xid;
		this.secs = secs;
		this.flags = flags;
		this.ciaddr = addresses.ciaddr;
		this.yiaddr = addresses.yiaddr;
		this.siaddr = addresses.siaddr;
		this.giaddr = addresses.giaddr;
		var macbytes = addresses.mac.getBytes();
		System.arraycopy(macbytes, 0, chaddr, 0, 6);
		this.options = options;
	}
	
	public DHCPMessage(byte[] bytes) {
		this.op = bytes[0];
		this.htype = bytes[1];
		this.hlen = bytes[2];
		this.hops = bytes[3];
		this.xid = ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) | ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
		this.secs = (short) (((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF));
		this.flags = (short) (((bytes[10] & 0xFF) << 8) | (bytes[11] & 0xFF));
		this.ciaddr = new IPAddress(bytes[12], bytes[13], bytes[14], bytes[15]);
		this.yiaddr = new IPAddress(bytes[16], bytes[17], bytes[18], bytes[19]);
		this.siaddr = new IPAddress(bytes[20], bytes[21], bytes[22], bytes[23]);
		this.giaddr = new IPAddress(bytes[24], bytes[25], bytes[26], bytes[27]);
		System.arraycopy(bytes, 28, this.chaddr, 0, 16);
		this.magicCookie = ((bytes[44] & 0xFF) << 24) | ((bytes[45] & 0xFF) << 16) | ((bytes[46] & 0xFF) << 8) | (bytes[47] & 0xFF);
		// Parse options
		int offset = DHCP_HEADER_SIZE + DHCP_BOOTP_SIZE + DHCP_MAGIC_SIZE;
		while(offset < bytes.length && bytes[offset] != DHCP_OPTION_END) {
			byte code = bytes[offset];
			if(offset + 1 >= bytes.length) {
				break; // Not enough bytes for length field
			}
			byte length = bytes[offset + 1];
			if(offset + 2 + length > bytes.length) {
				break; // Not enough bytes for data field
			}
			byte[] data = new byte[length];
			System.arraycopy(bytes, offset + 2, data, 0, length);
			DHCPOption option = new DHCPOption(code, length, data);
			options.add(option);
			offset += 2 + length;
		}
	}
	
	public byte getOp() {
		return op;
	}
	
	public byte getHtype() {
		return htype;
	}
	
	public byte getHlen() {
		return hlen;
	}
	
	public byte getHops() {
		return hops;
	}
	
	public int getXid() {
		return xid;
	}	
	
	public short getSecs() {
		return secs;
	}
	
	public short getFlags() {
		return flags;
	}
	
	public IPAddress getCiaddr() {
		return ciaddr;
	}
	
	public IPAddress getYiaddr() {
		return yiaddr;
	}
	
	public IPAddress getSiaddr() {
		return siaddr;
	}
	
	public IPAddress getGiaddr() {
		return giaddr;
	}
	
	public byte[] getChaddr() {
		return chaddr.clone();
	}
	
	public MACAddress getMacAddress() {
		byte[] macBytes = new byte[6];
		System.arraycopy(chaddr, 0, macBytes, 0, 6);
		return new MACAddress(macBytes);
	}
	
	public List<DHCPOption> getOptions() {
		return options;
	}
	
	@Override
	public byte[] getBytes() {
		int length = DHCP_HEADER_SIZE + DHCP_BOOTP_SIZE + DHCP_MAGIC_SIZE;
		for (DHCPOption option : options) {
			length += option.getTotalLength();
		}
		length += 1; // for the end option
		byte[] bytes = new byte[length]; 

		bytes[0] = op;
		bytes[1] = htype;
		bytes[2] = hlen;
		bytes[3] = hops;
		bytes[4] = (byte) ((xid >> 24) & 0xFF);
		bytes[5] = (byte) ((xid >> 16) & 0xFF);
		bytes[6] = (byte) ((xid >> 8) & 0xFF);
		bytes[7] = (byte) (xid & 0xFF);
		bytes[8] = (byte) ((secs >> 8) & 0xFF);
		bytes[9] = (byte) (secs & 0xFF);
		bytes[10] = (byte) ((flags >> 8) & 0xFF);
		bytes[11] = (byte) (flags & 0xFF);
		System.arraycopy(ciaddr.getBytes(), 0, bytes, 12, 4);
		System.arraycopy(yiaddr.getBytes(), 0, bytes, 16, 4);
		System.arraycopy(siaddr.getBytes(), 0, bytes, 20, 4);
		System.arraycopy(giaddr.getBytes(), 0, bytes, 24, 4);
		System.arraycopy(chaddr, 0, bytes, 28, 16);
		bytes[44] = (byte) ((magicCookie >> 24) & 0xFF);
		bytes[45] = (byte) ((magicCookie >> 16) & 0xFF);
		bytes[46] = (byte) ((magicCookie >> 8) & 0xFF);
		bytes[47] = (byte) (magicCookie & 0xFF);
		int offset = DHCP_HEADER_SIZE + DHCP_BOOTP_SIZE + DHCP_MAGIC_SIZE;
		for(DHCPOption option :options ) {
			System.arraycopy(option.getBytes(), 0, bytes, offset, option.getTotalLength());
			offset += option.getTotalLength();
		}
		bytes[offset] = (byte) DHCP_OPTION_END;

		return bytes;
	}

	/**
	 * Empty.
	 * @return a zero length byte array
	 */
	@Override
	public byte[] getContents() {
		return new byte[0];
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
