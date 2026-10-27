package org.singsurf.jns3.generic_protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;

class TestProtocol {
	
	// foo
	
	
	static class TypeA implements ProtocolType {
		String message;
		
		public TypeA(String message) {
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
		@Override
		public boolean isValid() {
			return true;
		}
	}

	
	static class TypeB implements ProtocolType {
		String message;
		
		public TypeB(String message) {
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
		@Override
		public boolean isValid() {
			return true;
		}
	}
	
	
	static class TypeC implements ProtocolType {
		String message;
		
		public TypeC(String message) {
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
		@Override
		public boolean isValid() {
			return true;
		}
	}


	static class TypeD implements ProtocolType {
		String message;
		
		public TypeD(String message) {
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
			return "D["+message+"]";
		}
		@Override
		public boolean isValid() {
			return true;
		}
	}

	static class ProtocolA implements UpperPort<TypeA> {

		ProtocolSender<TypeA> senderA = new ALoggingSender();
		ProtocolDelegator<TypeA> pdA = new ProtocolDelegator<>("pda");

		class ALoggingSender implements ProtocolSender<TypeA> {

			@Override
			public void send(TypeA a) {
				Jns3Logger.logInfo("ALoggingSender", "Message extracted: " + a);
			}
		}
		
		@Override
		public void register(LowerAdapter<TypeA> port) {
			for(var h: port.getInboundHandlers()) {
				pdA.addHandler(h);
			}
			port.setOutboundSender(senderA);
		}
		
		public void inbound(String message) {
			var packed = new TypeA(message);
			pdA.receive(packed);
		}
	}
	

	static class ProtocolB implements UpperPort<TypeB>, LowerAdapter<TypeA> {		
		ProtocolSender<TypeA> senderA;
		ProtocolReceiver<TypeA> callbackA;
		ProtocolDelegator<TypeB> delegatorB = new ProtocolDelegator<>("pdb");
		ABConvertor converterAB = new ABConvertor();
		ABLogicHandler logicHandlerAB = new ABLogicHandler();
		BALogic logicBA = new BALogic();
		ABLogic logicAB = new ABLogic();

		static class ABConvertor implements ProtocolConverter<TypeA, TypeB> {

			@Override
			public TypeB extract(TypeA a) {
				return  new TypeB(a.message.substring(1));
			}

			@Override
			public TypeA encapsulate(TypeB type) {
				return new TypeA("A"+type.message);
			}
			
		}

		class BALogic implements ProtocolSender<TypeB> {

			@Override
			public void send(TypeB b) {
				var s = b.message.toUpperCase();
				b.message = s;
				var a = converterAB.encapsulate(b);
				senderA.send(a);
			}
		}

		class ABLogicHandler implements ProtocolHandler<TypeA> {
			
			public boolean matches(TypeA message) {
				return message.message.charAt(1) == 'B';
			}

			@Override
			public Status handle(TypeA message) {
				if(!matches(message)) {
					return ProtocolHandler.IGNORED;
				}
				TypeB b = converterAB.extract(message);
				logicAB.process(message, b);
				return ProtocolHandler.HANDLED;
			}

		}

		class ABLogic implements InboundLogic<TypeA,TypeB> {

			@Override
			public void process(TypeA container, TypeB extracted) {
				var s = extracted.message.substring(1).toLowerCase();
				extracted.message = s;
				delegatorB.receive(extracted);
			}	
		}



		@Override
		public void register(LowerAdapter<TypeB> adapter) {
			for(var h: adapter.getInboundHandlers()) {
				delegatorB.addHandler(h);
			}
			adapter.setOutboundSender(logicBA);
			adapter.setInboundCallback(delegatorB);
		}

		@Override
		public List<ProtocolHandler<TypeA>> getInboundHandlers() {
			return List.of(logicHandlerAB);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeA> s) {
			senderA = s;			
		}

		@Override
		public void setInboundCallback(ProtocolReceiver<TypeA> r) {
			callbackA = r;
		}

	}
	
	
	static class ProtocolC implements LowerAdapter<TypeB> {
		ProtocolSender<TypeB> senderB;
		BCLoggingHandler loggerBC = new BCLoggingHandler();
		BCConvertor converterBC = new BCConvertor();
		CBTransmitter transCB = new CBTransmitter();

		class BCLoggingHandler implements ProtocolHandler<TypeB> {

			public boolean matches(TypeB message) {
				return message.message.charAt(0) == 'c';
			}


			@Override
			public Status handle(TypeB message) {
				if(!matches(message)) {
					return ProtocolHandler.IGNORED;
				}
				TypeC c = converterBC.extract(message);
				Jns3Logger.logInfo("BCHandler", "Message extracted: " + c);
				return ProtocolHandler.HANDLED;
			}

		}
			
		static class BCConvertor implements ProtocolConverter<TypeB, TypeC> {

			@Override
			public TypeC extract(TypeB b) {
				return  new TypeC(b.message.substring(1));
			}

			@Override
			public TypeB encapsulate(TypeC c) {
				return new TypeB("B"+c.message);
			}
			
		}


		class CBTransmitter implements ProtocolSender<TypeC> {
			@Override
			public void send(TypeC c) {
				var b = converterBC.encapsulate(c);
				senderB.send(b);
			}
		}

		@Override
		public List<ProtocolHandler<TypeB>> getInboundHandlers() {
			return List.of(loggerBC);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeB> s) {
			senderB = s;
		}


		public void outbound(String message) {
			var packed = new TypeC(message);
			transCB.send(packed);
		}
		
		@Override
		public void setInboundCallback(ProtocolReceiver<TypeB> r) {
		}


	}

	static class ProtocolD implements LowerAdapter<TypeB> {
		ProtocolSender<TypeB> senderB;
		BDLoggingHandler loggerBD = new BDLoggingHandler();
		BDConvertor converterBD = new BDConvertor();
		DBTransmitter transDB = new DBTransmitter();

		class BDLoggingHandler implements ProtocolHandler<TypeB> {

			public boolean matches(TypeB message) {
				return message.message.charAt(0) == 'd';
			}

			@Override
			public Status handle(TypeB message) {
				if(!matches(message)) {
					return ProtocolHandler.IGNORED;
				}
				TypeD d = converterBD.extract(message);
				Jns3Logger.logInfo("BDHandler", "Message extracted: " + d);
				return ProtocolHandler.HANDLED;
			}
		}
			
		static class BDConvertor implements ProtocolConverter<TypeB, TypeD> {

			@Override
			public TypeD extract(TypeB b) {
				return  new TypeD(b.message.substring(1));
			}

			@Override
			public TypeB encapsulate(TypeD d) {
				return new TypeB("B"+d.message);
			}
			
		}

		class DBTransmitter implements ProtocolSender<TypeD> {
			@Override
			public void send(TypeD d) {
				var b = converterBD.encapsulate(d);
				senderB.send(b);
			}
		}

		// protocol level methods

		@Override
		public List<ProtocolHandler<TypeB>> getInboundHandlers() {
			return List.of(loggerBD);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeB> s) {
			senderB = s;
		}

		@Override
		public void setInboundCallback(ProtocolReceiver<TypeB> r) {
		}

		// Methods exposed to the PC/test environment
		public void outbound(String message) {
			var packed = new TypeD(message);
			transDB.send(packed);
		}

	}

	/**
	 * Changes message type to C and adds an explanation mark.
	 */
	static class ProtocolE implements LowerAdapter<TypeB> {
		ProtocolReceiver<TypeB> delegator;
		EManipHandler manipHandler = new EManipHandler();
		
		class EManipHandler implements ProtocolHandler<TypeB> {

			public boolean matches(TypeB message) {
				return message.message.charAt(0) == 'e';
			}

			@Override
			public Status handle(TypeB message) {
				if(!matches(message)) {
					if(!matches(message)) {
						return ProtocolHandler.IGNORED;
					}
				}
				var rest = message.message.substring(1);				
				message.message = 'c' + rest + "!";
				delegator.receive(message);
				return ProtocolHandler.HANDLED;
			}

		}
		@Override
		public List<ProtocolHandler<TypeB>> getInboundHandlers() {
			return List.of(manipHandler);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeB> s) {
			
		}
		
		@Override
		public void setInboundCallback(ProtocolReceiver<TypeB> r) {
			delegator = r;
		}
	}	
	

	/**
	 * Always fails.
	 */
	static class ProtocolF implements LowerAdapter<TypeB> {
		EManipHandler manipHandler = new EManipHandler();
		
		class EManipHandler implements ProtocolHandler<TypeB> {

			public boolean matches(TypeB message) {
				return message.message.charAt(0) == 'f';
			}

			@Override
			public Status handle(TypeB message) {
				if(!matches(message)) {
					if(!matches(message)) {
						return ProtocolHandler.IGNORED;
					}
				}
				return new ProtocolHandler.Invalid("Failed: "+message);
			}

		}
		@Override
		public List<ProtocolHandler<TypeB>> getInboundHandlers() {
			return List.of(manipHandler);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeB> s) {
			
		}
		@Override
		public void setInboundCallback(ProtocolReceiver<TypeB> r) {
		}
	}	

	/**
	 * Always fails.
	 */
	static class ProtocolG implements LowerAdapter<TypeB> {
		GDoublerHandler manipHandler = new GDoublerHandler();
		ProtocolReceiver<TypeB> delegator;
		class GDoublerHandler implements ProtocolHandler<TypeB> {

			public boolean matches(TypeB message) {
				return message.message.charAt(0) == 'g';
			}

			@Override
			public Status handle(TypeB message) {
				if(!matches(message)) {
					if(!matches(message)) {
						return ProtocolHandler.IGNORED;
					}
				}
				var msgC = new TypeB("c"+message.message.substring(1));
				var msgD = new TypeB("d"+message.message.substring(1));
				delegator.receive(msgC);
				delegator.receive(msgD);
				return ProtocolHandler.HANDLED;
			}

		}
		@Override
		public List<ProtocolHandler<TypeB>> getInboundHandlers() {
			return List.of(manipHandler);
		}

		@Override
		public void setOutboundSender(ProtocolSender<TypeB> s) {
			
		}
		
		@Override
		public void setInboundCallback(ProtocolReceiver<TypeB> r) {
			delegator = r;
		}

	}	

	ProtocolA protA;
	ProtocolB protB;
	ProtocolC protC;
	ProtocolD protD;
	ProtocolE protE;
	ProtocolF protF;
	ProtocolG protG;
	

	@BeforeEach
	void setUp() throws Exception {
		protA = new ProtocolA();
		protB = new ProtocolB();
		protC = new ProtocolC();
		protD = new ProtocolD();
		protE = new ProtocolE();
		protF = new ProtocolF();
		protG = new ProtocolG();
		protA.register(protB);
		protB.register(protC);
		protB.register(protD);
		protB.register(protE);
		protB.register(protF);
		protB.register(protG);
	}


	@Test
	void logic_inbound() {
		// arrange
		Jns3Logger.clearLog();
		// act
		protA.inbound("ABCHello");
		// assert
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertEquals("[INFO] BCHandler: Message extracted: C[hello]", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

	
	@Test
	void sending_outbound() {
		Jns3Logger.clearLog();
		// act
		protC.outbound("And Welcome");
		// assert
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertEquals("[INFO] ALoggingSender: Message extracted: A[ABAND WELCOME]", 
				Jns3Logger.getItem(0).toString());

		assertTrue(Jns3Logger.isErrorFree());
	}


	@Test
	void D_inbound() {
		// arrange
		Jns3Logger.clearLog();
		// act
		protA.inbound("ABDHello");
		// assert
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertEquals("[INFO] BDHandler: Message extracted: D[hello]", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void D_outbound() {
		Jns3Logger.clearLog();
		// act
		protD.outbound("And Welcome");
		// assert
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertEquals("[INFO] ALoggingSender: Message extracted: A[ABAND WELCOME]", 
				Jns3Logger.getItem(0).toString());

		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void protE_manipulates_inbound() {
		// arrange
		Jns3Logger.clearLog();
		// act
		protA.inbound("ABEHello");
		// assert
		assertEquals(1, Jns3Logger.getLogItems().size());
		System.out.println(Jns3Logger.asString());
		assertEquals("[INFO] BCHandler: Message extracted: C[hello!]", Jns3Logger.getItem(0).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

	@Test
	void protF_always_fails() {
		// arrange
		Jns3Logger.clearLog();
		// act
		protA.inbound("ABFHello");
		// assert
		System.out.println(Jns3Logger.asString());
		assertEquals(1, Jns3Logger.getLogItems().size());
		assertEquals("[ERROR] pdb: Invalid message: Failed: B[fhello]", Jns3Logger.getItem(0).toString());
		assertFalse(Jns3Logger.isErrorFree());
	}

	@Test
	void protocolG_doubles_redirecting_to_C_and_D() {
		// arrange
		Jns3Logger.clearLog();
		// act
		protA.inbound("ABGHello");
		// assert
		System.out.println(Jns3Logger.asString());
		assertEquals(2, Jns3Logger.getLogItems().size());
		assertEquals("[INFO] BCHandler: Message extracted: C[hello]", Jns3Logger.getItem(0).toString());
		assertEquals("[INFO] BDHandler: Message extracted: D[hello]", Jns3Logger.getItem(1).toString());
		assertTrue(Jns3Logger.isErrorFree());
	}

}
