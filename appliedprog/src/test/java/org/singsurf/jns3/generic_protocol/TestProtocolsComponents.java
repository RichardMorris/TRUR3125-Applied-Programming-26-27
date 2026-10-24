package org.singsurf.jns3.generic_protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;

class TestProtocolsComponents {

	ABConvertor converterAB = new ABConvertor();
	BCConvertor converterBC = new BCConvertor();
	
	ABLoggingHandler loggingHandlerAB = new ABLoggingHandler();
	ABLogicHandler logicHandlerAB = new ABLogicHandler();
	BCLoggingHandler loggingHandlerBC = new BCLoggingHandler();
	ABLogic logicAB = new ABLogic();
	ProtocolDelegator<ProtocolA> pdA = new ProtocolDelegator<ProtocolA>("pda");
	ProtocolDelegator<ProtocolB> pdB = new ProtocolDelegator<ProtocolB>("pdb");

	CBLogic logicCB = new CBLogic();
	BATransmitter tranmitterBA = new BATransmitter();
	ALoggingSender loggingSenderA = new ALoggingSender();
	
	
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
			return "B["+message+"]";
		}
	}
	
	
	static class ProtocolC implements ProtocolType {
		String message;
		
		public ProtocolC(String message) {
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
			return "C["+message+"]";
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

	static class BCConvertor implements ProtocolConvertor<ProtocolB, ProtocolC> {

		@Override
		public ProtocolC extract(ProtocolB b) {
			return  new ProtocolC(b.message.substring(1));
		}

		@Override
		public ProtocolB encapsulate(ProtocolC type) {
			return new ProtocolB("B"+type.message);
		}
		
	}

	class ABLoggingHandler implements ProtocolHandler<ProtocolA> {

		public boolean matches(ProtocolA message) {
			return message.message.charAt(1) == 'B';
		}

		@Override
		public Status handle(ProtocolA message, ProtocolDelegator<ProtocolA> delegator) {
			if(!matches(message)) {
				return ProtocolHandler.IGNORED;
			}
			var b = converterAB.extract(message);
			Jns3Logger.logInfo("ABHandler", "Message extracted: " + b);
			return ProtocolHandler.HANDLED;
		}
	}
	
	class BCLoggingHandler implements ProtocolHandler<ProtocolB> {

		public boolean matches(ProtocolB message) {
			return message.message.charAt(0) == 'c';
		}

		@Override
		public Status handle(ProtocolB message, ProtocolDelegator<ProtocolB> delegator) {
			if(!matches(message)) {
				return ProtocolHandler.IGNORED;
			}
			ProtocolC c = converterBC.extract(message);
			Jns3Logger.logInfo("BCHandler", "Message extracted: " + c);
			return ProtocolHandler.HANDLED;
		}

	}

	class ABLogicHandler implements ProtocolHandler<ProtocolA> {
		InboundLogic<ProtocolA,ProtocolB> logic;
		
		public boolean matches(ProtocolA message) {
			return message.message.charAt(1) == 'B';
		}

		@Override
		public Status handle(ProtocolA message, ProtocolDelegator<ProtocolA> delegator) {
			if(!matches(message)) {
				return ProtocolHandler.IGNORED;
			}
			ProtocolB b = converterAB.extract(message);
			logic.handle(message, b);
			return ProtocolHandler.HANDLED;
		}

	}

	class ABLogic implements InboundLogic<ProtocolA,ProtocolB> {
		ProtocolReceiver<ProtocolB> receiver;

		@Override
		public void handle(ProtocolA container, ProtocolB extracted) {
			var s = extracted.message.substring(1).toLowerCase();
			extracted.message = s;
			receiver.receive(extracted);
		}	
	}

	class CBLogic implements ProtocolSender<ProtocolC> {
		ProtocolSender<ProtocolB> sender;

		@Override
		public void send(ProtocolC c) {
			var s = c.message.toUpperCase();
			c.message = s;
			var b = converterBC.encapsulate(c);
			sender.send(b);
		}
	}

	class BATransmitter implements ProtocolSender<ProtocolB> {
		ProtocolSender<ProtocolA> sender;

		@Override
		public void send(ProtocolB b) {
			var a = converterAB.encapsulate(b);
			sender.send(a);
		}
	}

	class ALoggingSender implements ProtocolSender<ProtocolA> {

		@Override
		public void send(ProtocolA a) {
			Jns3Logger.logInfo("ALoggingSender", "Message extracted: " + a);
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
		assertTrue(loggingHandlerAB.matches(a));
		var res = loggingHandlerAB.handle(a,pdA);
		assertEquals(ProtocolHandler.HANDLED, res);
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertTrue(Jns3Logger.isErrorFree());
		
		Jns3Logger.clearLog();
		var a2 = new ProtocolA("ACHello");
		assertFalse(loggingHandlerAB.matches(a2));
		
		res = loggingHandlerAB.handle(a2,pdA);
		assertEquals(ProtocolHandler.IGNORED, res);
		assertEquals(0, Jns3Logger.getLogItems().size());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void delegate_inbound() {
		pdA.addHandler(loggingHandlerAB);

		Jns3Logger.clearLog();
		var a = new ProtocolA("ABHello");
		pdA.receive(a);
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		
		Jns3Logger.clearLog();
		var a2 = new ProtocolA("ACHello");		
		pdA.receive(a2);
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertFalse(Jns3Logger.isErrorFree());
		assertEquals("pda",Jns3Logger.getItem(0).deviceName());
	}

	@Test
	void logic_inbound() {
		pdA.addHandler(logicHandlerAB);
		logicHandlerAB.logic = logicAB;
		logicAB.receiver = pdB;
		pdB.addHandler(loggingHandlerBC);
		Jns3Logger.clearLog();
		var a = new ProtocolA("ABCHello");
		pdA.receive(a);
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		assertEquals("[INFO] BCHandler: Message extracted: C[hello]", Jns3Logger.getItem(0).toString());
	}

	
	@Test
	void sending_outbound() {
		ProtocolSender<ProtocolC> csender = logicCB;
		logicCB.sender = tranmitterBA;
		tranmitterBA.sender = loggingSenderA;
		
		var c = new ProtocolC("And Welcome");
		Jns3Logger.clearLog();
		csender.send(c);

		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertTrue(Jns3Logger.isErrorFree());
		assertEquals("[INFO] ALoggingSender: Message extracted: A[ABAND WELCOME]", 
				Jns3Logger.getItem(0).toString());
	}

}
