package org.singsurf.jns3.generic_protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;

class TestSimpleProtocols {

	ABConvertor converterAB = new ABConvertor();
	ABHandler handlerAB = new ABHandler();
	
	static class ProtocolA implements ProtocolType {
		String message;
		
		public ProtocolA(String message) {
			this.message = message;
		}
		@Override
		public byte[] getBytes() {
			return message.getBytes();
		}

		@Override
		public byte[] getContents() {
			return message.substring(1).getBytes();
		}
		
		public String toString() {
			return "A["+message+"]";
		}
	}

	
	static class ProtocolB implements ProtocolType {
		String message;
		
		public ProtocolB(String message) {
			this.message = message;
		}
		@Override
		public byte[] getBytes() {
			return message.getBytes();
		}

		@Override
		public byte[] getContents() {
			return message.substring(1).getBytes();
		}
		public String toString() {
			return "A["+message+"]";
		}
	}
	
	static class ABConvertor implements ProtocolConvertor<ProtocolA, ProtocolB> {

		@Override
		public ProtocolB extract(ProtocolA a) {
			return  new ProtocolB(a.message.substring(1));
		}

		@Override
		public ProtocolA encapsulate(ProtocolB type) {
			return new ProtocolA("A"+type.message);
		}
		
	}
	
	class ABHandler implements ProtocolHandler<ProtocolA> {

		@Override
		public boolean matches(ProtocolA message) {
			return message.message.charAt(1) == 'B';
		}

		@Override
		public ProtocolA handle(ProtocolA message) {
			if(!matches(message)) {
				Jns3Logger.logError("ABHandler", "Message does not match: " + message);
				return message;
			}
			var b = converterAB.extract(message);
			Jns3Logger.logInfo("ABHandler", "Message extracted: " + b);
			return null;
		}
		
	}
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void inbound_conversion() {
		var a = new ProtocolA("AHello");
		var b = converterAB.extract(a);
		if(!b.message.equals("Hello")) {
			fail("Conversion failed");
		}
	}
	
	@Test
	void outbound_conversion() {
		var b = new ProtocolB("Hello");
		var a = converterAB.encapsulate(b);
		if(!a.message.equals("AHello")) {
			fail("Conversion failed");
		}
	}

	@Test
	void handle_inbound() {
		Jns3Logger.clearLog();
		var a = new ProtocolA("ABHello");
		assertTrue(handlerAB.matches(a));
		var res = handlerAB.handle(a);
		assertNull(res);
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertTrue(Jns3Logger.isErrorFree());
		
		Jns3Logger.clearLog();
		var a2 = new ProtocolA("ACHello");
		assertFalse(handlerAB.matches(a2));
		
		res = handlerAB.handle(a2);
		assertNotNull(res);
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertFalse(Jns3Logger.isErrorFree());
	}

}
