package org.singsurf.jns3.network_layer;

public class IPPrefix {
	SubnetMask mask;
	IPAddress prefix;
	int prefixLength;
	
	public IPPrefix(IPAddress prefix, int prefixLength) {
		this.prefix = prefix;
		this.prefixLength = prefixLength;
		this.mask = switch(prefixLength) {
		case 0 -> new SubnetMask("0.0.0.0");
		case 1 -> new SubnetMask("128.0.0.0");
		case 2 -> new SubnetMask("192.0.0.0");
		case 3 -> new SubnetMask("224.0.0.0");
		case 4 -> new SubnetMask("240.0.0.0");
		case 5 -> new SubnetMask("248.0.0.0");
		case 6 -> new SubnetMask("252.0.0.0");
		case 7 -> new SubnetMask("254.0.0.0");
		case 8 -> new SubnetMask("255.0.0.0");
		case 9 -> new SubnetMask("255.128.0.0");
		case 10 -> new SubnetMask("255.192.0.0");
		case 11 -> new SubnetMask("255.224.0.0");
		case 12 -> new SubnetMask("255.240.0.0");
		case 13 -> new SubnetMask("255.248.0.0");
		case 14 -> new SubnetMask("255.252.0.0");
		case 15 -> new SubnetMask("255.254.0.0");
		case 16 -> new SubnetMask("255.255.0.0");
		case 17 -> new SubnetMask("255.255.128.0");
		case 18 -> new SubnetMask("255.255.192.0");
		case 19 -> new SubnetMask("255.255.224.0");
		case 20 -> new SubnetMask("255.255.240.0");
		case 21 -> new SubnetMask("255.255.248.0");
		case 22 -> new SubnetMask("255.255.252.0");
		case 23 -> new SubnetMask("255.255.254.0");
		case 24 -> new SubnetMask("255.255.255.0");
		case 25 -> new SubnetMask("255.255.255.128");
		case 26 -> new SubnetMask("255.255.255.192");
		case 27 -> new SubnetMask("255.255.255.224");
		case 28 -> new SubnetMask("255.255.255.240");
		case 29 -> new SubnetMask("255.255.255.248");
		case 30 -> new SubnetMask("255.255.255.252");
		case 31 -> new SubnetMask("255.255.255.254");
		case 32 -> new SubnetMask("255.255.255.255");
		
		default -> throw new IllegalArgumentException("Unsupported prefix length: " + prefixLength);
		};
	}
	
	public IPPrefix(String prefixStr) {
		this(prefixStr.split("/"));
	}
	
	
	private IPPrefix(String[] split) {
		this(new IPAddress(split[0]), Integer.parseInt(split[1]));
		if(split.length != 2)
		throw new IllegalArgumentException("Invalid prefix format: " + String.join("/", split));
	}

	public boolean matches(IPAddress addr) {
		return mask.matches(prefix, addr);
	}

	public SubnetMask getSubnetMask() {
		return mask;
	}

	public IPAddress getPrefix() {
		return prefix;
	}

	public int getPrefixLength() {
		return prefixLength;
	}

	
}
