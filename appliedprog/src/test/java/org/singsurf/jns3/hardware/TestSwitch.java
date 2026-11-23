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

public class TestSwitch {

    @Nested
    class TwoPCnetwork {
    private PC pc1;
    private PC pc2;
    private Switch sw;

    @BeforeEach
        public void setup() {
            Jns3Logger.clearLog();
            DeviceFactory deviceFactory = new DeviceFactory();
            pc1 = deviceFactory.createPC("PC1");
            pc2 = deviceFactory.createPC("PC2");
            sw = deviceFactory.createSwitch("Switch1", 8);

            var connection1 = new SimpleConnection(pc1.getEndPoint(), sw.getEndPoint(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), sw.getEndPoint(1));
            assertNotNull(connection1);
            assertNotNull(connection2);
            assertEquals(Jns3Logger.getLogItems().size(), 0);
        }

        @Test
        public void testSwitchConnections() {
            assertTrue(sw.getPort(0).isConnected());
            assertTrue(sw.getPort(1).isConnected());
            assertFalse(sw.getPort(2).isConnected());
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
            var destMAC = pc1.lookup(destIP);
            assertNull(destMAC);

            pc1.arpController.sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(4      , logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.lookup(destIP);
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

            destMAC = pc2.lookup(pc1.getIPAddress());
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
        private Switch sw;

        @BeforeEach
        public void setup() {
            Jns3Logger.clearLog();
            DeviceFactory deviceFactory = new DeviceFactory();
            pc1 = deviceFactory.createPC("PC1");
            pc2 = deviceFactory.createPC("PC2");
            pc3 = deviceFactory.createPC("PC3");
            sw = deviceFactory.createSwitch("Switch1", 8);

            var connection1 = new SimpleConnection(pc1.getEndPoint(), sw.getEndPoint(0));
            var connection2 = new SimpleConnection(pc2.getEndPoint(), sw.getEndPoint(1));
            var connection3 = new SimpleConnection(pc3.getEndPoint(), sw.getEndPoint(5));
            assertNotNull(connection1);
            assertNotNull(connection2);
            assertNotNull(connection3);
            assertEquals(Jns3Logger.getLogItems().size(), 0);
        }

        @Test
        public void testSwitchConnections() {
            assertTrue(sw.getPort(0).isConnected());
            assertTrue(sw.getPort(1).isConnected());
            assertTrue(sw.getPort(5).isConnected());
            assertFalse(sw.getPort(2).isConnected());
        }   

        @Test 
        public void connection_to_specified_mac_address_is_sent_to_all() {
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
            var destMAC = pc1.lookup(destIP);
            assertNull(destMAC);

            pc1.arpController.sendArpRequest(destIP);

            List<LogItem> logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(7, logs.size());

            Jns3Logger.clearLog();

            destMAC = pc1.lookup(destIP);
            assertNotNull(destMAC);
            assertEquals(pc2.getMACAddress(), destMAC);

            var frame = pc1.createEthernetFrame(
                destMAC, 
                "Hello PC2 from PC1");
            pc1.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            var log0 = logs.get(0);

            assertEquals(Jns3Logger.LogLevel.RECEIVED  , log0.level);
            assertEquals("PC2", log0.deviceName);
            assertEquals("Hello PC2 from PC1", log0.message);


            // PC2 should have PC1's MAC address in its ARP table
            Jns3Logger.clearLog();

            destMAC = pc2.lookup(pc1.getIPAddress());
            assertNotNull(destMAC);
            assertEquals(pc1.getMACAddress(), destMAC);

            // PC2 sends a frame to PC1
            frame = pc2.createEthernetFrame(
                destMAC, 
                "Hello PC1 from PC2");
            pc2.sendFrame(frame);
            logs = Jns3Logger.getLogItems();
            System.out.println(logs);
            assertEquals(1, logs.size());
            log0 = logs.get(0);

            assertEquals(Jns3Logger.LogLevel.RECEIVED  , log0.level);
            assertEquals("PC1", log0.deviceName);
            assertEquals("Hello PC1 from PC2", log0.message);



        }

    }

}
