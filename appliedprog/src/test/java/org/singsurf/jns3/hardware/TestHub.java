package org.singsurf.jns3.hardware;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.singsurf.jns3.Jns3Logger;
import org.singsurf.jns3.Jns3Logger.LogItem;

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

            var connection1 = new SimpleConnection(pc1.getEndPoint(), hub.getPort(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), hub.getPort(1));
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
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello All from PC1");
            pc1.sendFrame(frame);
            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            assertEquals(Jns3Logger.LogLevel.RECEIVED  , logs.get(0).level);
            assertEquals("PC2", logs.get(0).deviceName);
            assertEquals("Hello All from PC1", logs.get(0).message);
        }

        @Test 
        public void connection_to_unknown_mac_address() {
            Jns3Logger.clearLog();
            var destIP = pc2.getIPAddress();
            var destMAC = pc1.arpController.getArpTable().get(destIP);
            assertNull(destMAC);

            pc1.arpController.sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(4      , logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.arpController.getArpTable().get(destIP);
            assertNotNull(destMAC);
            assertEquals(pc2.getMACAddress(), destMAC);

            var frame = pc1.createEthernetFrame(
                destMAC, 
                "Hello PC2 from PC1");
            pc1.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            assertEquals(Jns3Logger.LogLevel.RECEIVED  , logs.get(0).level);
            assertEquals("PC2", logs.get(0).deviceName);
            assertEquals("Hello PC2 from PC1", logs.get(0).message);

            // PC2 should have PC1's MAC address in its ARP table
            Jns3Logger.clearLog();

            destMAC = pc2.arpController.getArpTable().get(pc1.getIPAddress());
            assertNotNull(destMAC);
            assertEquals(pc1.getMACAddress(), destMAC);

            frame = pc2.createEthernetFrame(
                destMAC, 
                "Hello PC1 from PC2");
            pc2.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            assertEquals(Jns3Logger.LogLevel.RECEIVED  , logs.get(0).level);
            assertEquals("PC1", logs.get(0).deviceName);
            assertEquals("Hello PC1 from PC2", logs.get(0).message);


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

            var connection1 = new SimpleConnection(pc1.getEndPoint(), hub.getPort(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), hub.getPort(1));
            var connection3 = new SimpleConnection(pc3.getEndPoint(), hub.getPort(5));
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
            var frame = pc1.createEthernetFrame(
                pc2.getMACAddress(), 
                "Hello All from PC1");
            pc1.sendFrame(frame);
            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());
            var log0 = logs.get(0);
            var log1 = logs.get(1);
            assertEquals(Jns3Logger.LogLevel.RECEIVED  , log0.level);
            assertEquals("PC2", log0.deviceName);
            assertEquals("Hello All from PC1", log0.message);
            assertEquals(Jns3Logger.LogLevel.IGNORED  , log1.level);
            assertEquals("PC3", log1.deviceName);
            assertEquals("Hello All from PC1", log1.message);   
        }

        @Test 
        public void connection_to_unknown_mac_address() {
            Jns3Logger.clearLog();
            var destIP = pc2.getIPAddress();
            var destMAC = pc1.arpController.getArpTable().get(destIP);
            assertNull(destMAC);

            pc1.arpController.sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(9      , logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.arpController.getArpTable().get(destIP);
            assertNotNull(destMAC);
            assertEquals(pc2.getMACAddress(), destMAC);

            var frame = pc1.createEthernetFrame(
                destMAC, 
                "Hello PC2 from PC1");
            pc1.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());
            var log0 = logs.get(0);
            var log1 = logs.get(1);

            assertEquals(Jns3Logger.LogLevel.RECEIVED  , log0.level);
            assertEquals("PC2", log0.deviceName);
            assertEquals("Hello PC2 from PC1", log0.message);

            assertEquals(Jns3Logger.LogLevel.IGNORED  , log1.level);
            assertEquals("PC3", log1.deviceName);
            assertEquals("Hello PC2 from PC1", log1.message);

            // PC2 should have PC1's MAC address in its ARP table
            Jns3Logger.clearLog();

            destMAC = pc2.arpController.getArpTable().get(pc1.getIPAddress());
            assertNotNull(destMAC);
            assertEquals(pc1.getMACAddress(), destMAC);

            frame = pc2.createEthernetFrame(
                destMAC, 
                "Hello PC1 from PC2");
            pc2.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(2, logs.size());
            log0 = logs.get(0);
            log1 = logs.get(1);

            assertEquals(Jns3Logger.LogLevel.RECEIVED  , log0.level);
            assertEquals("PC1", log0.deviceName);
            assertEquals("Hello PC1 from PC2", log0.message);

            assertEquals(Jns3Logger.LogLevel.IGNORED  , log1.level);
            assertEquals("PC3", log1.deviceName);
            assertEquals("Hello PC1 from PC2", log1.message);


        }

    }

}
