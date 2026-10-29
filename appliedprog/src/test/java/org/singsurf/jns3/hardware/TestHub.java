package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;
import org.singsurf.jns3.data_link_layer.EthernetFrame;
import org.singsurf.jns3.physical_layer.SimpleConnection;

public class TestHub {

    @Nested
    class TwoPCnetwork {
    private PC pc1;
    private PC pc2;
    private Hub hub;

    @BeforeEach
        public void setup() {
            Jns3Logger.clearLog();
            DeviceFactory deviceFactory = new DeviceFactory();
            pc1 = deviceFactory.createPC("PC1");
            pc2 = deviceFactory.createPC("PC2");
            hub = deviceFactory.createHub("Hub1", 8);

            var connection1 = new SimpleConnection(pc1.getEndPoint(), hub.getEndPoint(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), hub.getEndPoint(1));
            assertNotNull(connection1);
            assertNotNull(connection2);
            assertEquals(Jns3Logger.getLogItems().size(), 0);
        }

        @Test
        public void testHubConnections() {
            assertTrue(hub.getPort(0).isConnected());
            assertTrue(hub.getPort(1).isConnected());
            assertFalse(hub.getPort(2).isConnected());
        }   

        @Test 
        public void connection_to_known_mac_address() {
            Jns3Logger.clearLog();
			byte[] payload = "Hello All from PC1".getBytes();
			short etherType = (short) payload.length;
            var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
            pc1.getEthernetProtocol().getFrameSender().send(frame);
            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            assertEquals(new Jns3Logger.LogItem("PC2", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello All from PC1"), 
            	logs.get(0));
        }

        @Test 
        public void connection_to_unknown_mac_address() {
            Jns3Logger.clearLog();
            var destIP = pc2.getIPAddress();
            var destMAC = pc1.getArpProtocol().lookup(destIP);
            assertNull(destMAC);

            pc1.getArpProtocol().sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(4      , logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.getArpProtocol().lookup(destIP);
            assertNotNull(destMAC);
            assertEquals(pc2.getMACAddress(), destMAC);
			byte[] payload = "Hello PC2 from PC1".getBytes();
			short etherType = (short) payload.length;

            var frame = new EthernetFrame(destMAC, pc1.getMACAddress(), etherType, payload);
            pc1.getEthernetProtocol().getFrameSender().send(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            assertEquals(new Jns3Logger.LogItem("PC2", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello PC2 from PC1"), 
            	logs.get(0));

            // PC2 should have PC1's MAC address in its ARP table
            Jns3Logger.clearLog();

            destMAC = pc2.getArpProtocol().lookup(pc1.getIPAddress());
            assertNotNull(destMAC);
            assertEquals(pc1.getMACAddress(), destMAC);
			byte[] payload1 = "Hello PC1 from PC2".getBytes();
			short etherType1 = (short) payload1.length;

            frame = new EthernetFrame(destMAC, pc2.getMACAddress(), etherType1, payload1);
            pc2.getEthernetProtocol().getFrameSender().send(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            
            assertEquals(new Jns3Logger.LogItem("PC1", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello PC1 from PC2"), 
            	logs.get(0));
        }

    }

    @Nested
    class ThreePCnetwork {
        private PC pc1;
        private PC pc2;
        private PC pc3;
        private Hub hub;

        @BeforeEach
        public void setup() {
            Jns3Logger.clearLog();
            DeviceFactory deviceFactory = new DeviceFactory();
            pc1 = deviceFactory.createPC("PC1");
            pc2 = deviceFactory.createPC("PC2");
            pc3 = deviceFactory.createPC("PC3");
            hub = deviceFactory.createHub("Hub1", 8);

            var connection1 = new SimpleConnection(pc1.getEndPoint(), hub.getEndPoint(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), hub.getEndPoint(1));
            var connection3 = new SimpleConnection(pc3.getEndPoint(), hub.getEndPoint(5));
            assertNotNull(connection1);
            assertNotNull(connection2);
            assertNotNull(connection3);
            assertEquals(Jns3Logger.getLogItems().size(), 0);
        }

        @Test
        public void testHubConnections() {
            assertTrue(hub.getPort(0).isConnected());
            assertTrue(hub.getPort(1).isConnected());
            assertTrue(hub.getPort(5).isConnected());
            assertFalse(hub.getPort(2).isConnected());
        }   

        @Test 
        public void connection_to_known_mac_address() {
            Jns3Logger.clearLog();
			byte[] payload = "Hello All from PC1".getBytes();
			short etherType = (short) payload.length;
            var frame = new EthernetFrame(pc2.getMACAddress(), pc1.getMACAddress(), etherType, payload);
            pc1.getEthernetProtocol().getFrameSender().send(frame);
            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());
            
            assertEquals(new Jns3Logger.LogItem("PC2", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello All from PC1"), 
            	logs.get(0));

            assertEquals(new Jns3Logger.LogItem("PC3", 
            		Jns3Logger.LogLevel.IGNORED,
            		"Hello All from PC1"), 
            	logs.get(1));
        }

        @Test 
        public void connection_to_unknown_mac_address() {
            Jns3Logger.clearLog();
            var destIP = pc2.getIPAddress();
            var destMAC = pc1.getArpProtocol().lookup(destIP);
            assertNull(destMAC);

            pc1.getArpProtocol().sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(9      , logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.getArpProtocol().lookup(destIP);
            assertNotNull(destMAC);
            assertEquals(pc2.getMACAddress(), destMAC);
			byte[] payload = "Hello PC2 from PC1".getBytes();
			short etherType = (short) payload.length;

            var frame = new EthernetFrame(destMAC, pc1.getMACAddress(), etherType, payload);
            pc1.getEthernetProtocol().getFrameSender().send(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());

            assertEquals(new Jns3Logger.LogItem("PC2", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello PC2 from PC1"), 
            	logs.get(0));

            assertEquals(new Jns3Logger.LogItem("PC3", 
            		Jns3Logger.LogLevel.IGNORED,
            		"Hello PC2 from PC1"), 
            	logs.get(1));

            // PC2 should have PC1's MAC address in its ARP table
            Jns3Logger.clearLog();

            destMAC = pc2.getArpProtocol().lookup(pc1.getIPAddress());
            assertNotNull(destMAC);
            assertEquals(pc1.getMACAddress(), destMAC);
			byte[] payload1 = "Hello PC1 from PC2".getBytes();
			short etherType1 = (short) payload1.length;

            frame = new EthernetFrame(destMAC, pc2.getMACAddress(), etherType1, payload1);
            pc2.getEthernetProtocol().getFrameSender().send(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());

            assertEquals(new Jns3Logger.LogItem("PC1", 
            		Jns3Logger.LogLevel.RECEIVED,
            		"Hello PC1 from PC2"), 
            	logs.get(0));

            assertEquals(new Jns3Logger.LogItem("PC3", 
            		Jns3Logger.LogLevel.IGNORED,
            		"Hello PC1 from PC2"), 
            	logs.get(1));
        }

    }

}
