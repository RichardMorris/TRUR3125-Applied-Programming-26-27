package org.singsurf.netsym.netswitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

public class TestSwitch {

    @Test 
    public void create_bad_bit_mask_9() {
        IPAddr addr = new IPAddr("138.0.0.0");
        assertThrows(IllegalArgumentException.class, ()->
            new IPSwitch(addr, 9));
    }

    @Nested 
    public class BitMask0Tests {
        @Test 
        public void create_bit_mask_0() {
            IPAddr addr = new IPAddr("0.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 0);
            assertNotNull(sw);
        }

        @Test 
        public void create_bad_bit_mask_0() {
            IPAddr addr = new IPAddr("138.0.0.0");
            assertThrows(IllegalArgumentException.class, ()->
                new IPSwitch(addr, 0));
        }

        @Test 
        public void get_self_bitmask_0() {
            IPAddr addr = new IPAddr("0.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 0);
            IPDevice dev = sw.getDevice(0);
            assertSame(sw, dev);
        }

        
        @Test 
        public void add_device_to_bitmask_0() {
            IPAddr addr = new IPAddr("0.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 0);

            IPAddr addr2 = new IPAddr("1.0.0.0");
            PC pc = new PC(addr2);

            sw.addDevice(pc);
            IPDevice dev = sw.getDevice(1);
            assertSame(pc, dev);

            dev = sw.getDevice(2);
            assertNull(dev);
        }

        @Test 
        public void add_bad_device_to_bitmask_0() {
            IPAddr addr = new IPAddr("0.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 0);

            IPAddr addr2 = new IPAddr("0.0.0.0");
            PC pc = new PC(addr2);

            assertThrows(IllegalArgumentException.class, ()->  
                sw.addDevice(pc));
        }

    }

    @Nested 
    public class BitMask8Tests {

        @Test 
        public void create_bit_mask_8() {
            IPAddr addr = new IPAddr("138.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 8);
            assertNotNull(sw);
        }

        @Test 
        public void create_bad_bit_mask_8() {
            IPAddr addr = new IPAddr("138.39.0.0");
            assertThrows(IllegalArgumentException.class, ()->
                new IPSwitch(addr, 8));
        }

        @Test 
        public void add_device_to_bitmask_8() {
            IPAddr addr = new IPAddr("138.0.0.0");
            IPSwitch sw = new IPSwitch(addr,8);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);
            IPDevice dev = sw.getDevice(253);
            assertSame(pc, dev);

            dev = sw.getDevice(2);
            assertNull(dev);
        }

        @Test 
        public void add_bad_device_to_bitmask_8() {
            IPAddr addr = new IPAddr("138.0.0.0");
            IPSwitch sw = new IPSwitch(addr, 8);

            IPAddr addr2 = new IPAddr("136.253.39.2");
            PC pc = new PC(addr2);

            assertThrows(IllegalArgumentException.class, ()->  
                sw.addDevice(pc));
        }

    }

    @Nested 
    public class BitMask16Tests {

        @Test 
        public void create_bit_mask_16() {
            IPAddr addr = new IPAddr("138.39.0.0");
            IPSwitch sw = new IPSwitch(addr, 16);
            assertNotNull(sw);
        }

        @Test 
        public void create_bad_bit_mask_16() {
            IPAddr addr = new IPAddr("138.39.2.0");
            assertThrows(IllegalArgumentException.class, ()->
                new IPSwitch(addr, 16));
        }

        @Test 
        public void add_device_to_bitmask_16() {
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);
            IPDevice dev = sw.getDevice(39);
            assertSame(pc, dev);

            dev = sw.getDevice(2);
            assertNull(dev);
        }

        @Test 
        public void add_conflicting_device() {
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);

            IPAddr addr3 = new IPAddr("138.253.39.3");
            PC pc3 = new PC(addr3);
            assertThrows(IllegalArgumentException.class, ()->
                sw.addDevice(pc3));

        }

        @Test 
        public void add_bad_device_to_bitmask_16() {
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr, 16);

            IPAddr addr2 = new IPAddr("138.254.38.2");
            PC pc = new PC(addr2);

            assertThrows(IllegalArgumentException.class, ()->  
                sw.addDevice(pc));
        }

        @Test 
        public void send_message_device_to_bitmask_16() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);

            Message msg = new Message(addr2, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "138.253.39.2: Hello world";
            assertEquals(expected, output.strip());         
        }

        @Test 
        public void send_bad_message_device_to_bitmask_16() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);

            IPAddr addr3 = new IPAddr("138.253.39.3");
            Message msg = new Message(addr3, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "Error: 138.253.39.2 != 138.253.39.3: Hello world";
            assertEquals(expected, output.strip());         
        }

        @Test 
        public void send_message_to_switch_bitmask_16() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr2 = new IPAddr("138.253.0.0");
            Message msg = new Message(addr2, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
                    ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "Switch Message 138.253.0.0 : Hello world";
            assertEquals(expected, output.strip());         
        }

        @Test 
        public void add_nested_switch_device() {
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr1 = new IPAddr("138.253.39.0");
            IPSwitch sw2 = new IPSwitch(addr1, 24);

            sw.addDevice(sw2);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw2.addDevice(pc);

            IPAddr addr3 = new IPAddr("138.253.39.3");
            PC pc3 = new PC(addr3);
            sw2.addDevice(pc3);
        }

        @Test 
        public void send_message_to_nested_device() {
            IPAddr addr = new IPAddr("138.253.0.0");
            IPSwitch sw = new IPSwitch(addr,16);

            IPAddr addr1 = new IPAddr("138.253.39.0");
            IPSwitch sw2 = new IPSwitch(addr1, 24);

            sw.addDevice(sw2);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw2.addDevice(pc);

            IPAddr addr3 = new IPAddr("138.253.39.3");
            PC pc3 = new PC(addr3);
            sw2.addDevice(pc3);


            Message msg = new Message(addr2, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "138.253.39.2: Hello world";
            assertEquals(expected, output.strip());         

        }


    }

    @Nested 
    public class BitMask24Tests {

        @Test 
        public void create_bit_mask_24() {
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr, 24);
            assertNotNull(sw);
        }

        @Test 
        public void create_bad_bit_mask_24() {
            IPAddr addr = new IPAddr("138.253.39.2");
            assertThrows(IllegalArgumentException.class, ()->
                new IPSwitch(addr, 24));
        }

        @Test 
        public void add_device_to_bitmask_24() {
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr,24);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);
            IPDevice dev = sw.getDevice(2);
            assertSame(pc, dev);
        }

        @Test 
        public void add_bad_device_to_bitmask_24() {
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr, 24);

            IPAddr addr2 = new IPAddr("136.253.39.0");
            PC pc = new PC(addr2);

            assertThrows(IllegalArgumentException.class, ()->  
                sw.addDevice(pc));
        }

        @Test 
        public void send_message_device_to_bitmask_24() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr,24);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);

            Message msg = new Message(addr2, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "138.253.39.2: Hello world";
            assertEquals(expected, output.strip());         
        }

        @Test 
        public void send_bad_message_device_to_bitmask_24() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr,24);

            IPAddr addr2 = new IPAddr("138.253.39.2");
            PC pc = new PC(addr2);

            sw.addDevice(pc);

            IPAddr addr3 = new IPAddr("138.253.39.3");
            Message msg = new Message(addr3, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "Error: no device for IP 138.253.39.3";
            assertEquals(expected, output.strip());         
        }

        @Test 
        public void send_message_to_switch_bitmask_24() {
            // Arrange
            IPAddr addr = new IPAddr("138.253.39.0");
            IPSwitch sw = new IPSwitch(addr,24);

            IPAddr addr2 = new IPAddr("138.253.39.0");
            Message msg = new Message(addr2, "Hello world");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            var oldout = System.out;
            System.setOut(ps);

            // Act
            sw.receive(msg);

            // Assert
            ps.close();
            System.setOut(oldout);
            
            String output = baos.toString();
            String expected = "Switch Message 138.253.39.0 : Hello world";
            assertEquals(expected, output.strip());         
        }

    }

}
