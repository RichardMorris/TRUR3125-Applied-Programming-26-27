package org.singsurf.jns3.ipswitch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class TestPC {
    @Test 
    public void create() {
        String s = "138.253.39.2";
        PC pc = new PC(new IPAddr(s));
        String s2 = pc.getAddr().toString();
        assertEquals(s, s2);
    }

    @Test
    public void receive_to_correct_address() {
        // Arrange
        String s = "138.253.39.2";
        PC pc = new PC(new IPAddr(s));
        IPMessage msg = new IPMessage(new IPAddr(s), "hello world");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        var oldout = System.out;
        System.setOut(ps);

        // Act
        pc.receive(msg);

        // Assert
        ps.close();
        System.setOut(oldout);
        
        String output = baos.toString();
        String expected = "138.253.39.2: hello world";
        assertEquals(expected, output.strip());         
    }

    @Test
    public void receive_to_incorrect_address() {
        // Arrange
        String s = "138.253.39.2";
        String s2 = "138.253.39.3";
        PC pc = new PC(new IPAddr(s));
        IPMessage msg = new IPMessage(new IPAddr(s2), "hello world");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        var oldout = System.out;
        System.setOut(ps);

        // Act
        pc.receive(msg);

        // Assert
        ps.close();
        System.setOut(oldout);
        
        String output = baos.toString();
        String expected = "Error: 138.253.39.2 != 138.253.39.3: hello world";
        assertEquals(expected, output.strip());         
    }

}
