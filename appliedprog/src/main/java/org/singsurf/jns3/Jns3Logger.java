package org.singsurf.jns3;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.jns3.network_layer.OnesComplementNumber;
public class Jns3Logger {
    public enum LogLevel {
        RECEIVED, // Frame was received successfully
        IGNORED, // Frame was ignored 
        ERROR, 
        SENT,
        INFO
    }
    public static record LogItem(String deviceName, LogLevel level, String message) {
        @Override
        public String toString() {
            return "[" + level + "] " + deviceName + ": " + message;
        }
    }

    static List<LogItem> logItems = new ArrayList<>();

    static public void log(String deviceName, LogLevel level, String message) {
        logItems.add(new LogItem(deviceName, level, message));
    }

    static public void logRec(String deviceName, String message) {
        logItems.add(new LogItem(deviceName, LogLevel.RECEIVED, message));
    }
    static public void logSend(String deviceName, String message) {
        logItems.add(new LogItem(deviceName, LogLevel.SENT, message));
    }
    static public void logIgn(String deviceName, String message) {
        logItems.add(new LogItem(deviceName, LogLevel.IGNORED, message));
    }
    static public void logError(String deviceName, String message) {
        logItems.add(new LogItem(deviceName, LogLevel.ERROR, message));
    }
	public static void logInfo(String name, String message) {
        logItems.add(new LogItem(name, LogLevel.INFO, message));
	}


    static public List<LogItem> getLogItems() {
        return logItems;
    }   

    static public List<LogItem> getErrorLogItems() {
        return logItems.stream().filter(item -> item.level == LogLevel.ERROR).toList();
    }   

    static public LogItem getItem(int index) {
		if(index < 0 || index >= logItems.size()) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + logItems.size());
		}	
		return logItems.get(index);
	}
    static public void clearLog() {
        logItems.clear();
    }

	public static boolean isErrorFree() {
		for(LogItem item : logItems) {
			if(item.level == LogLevel.ERROR) {
				return false;
			}
		}
		return true;
	}

	public static String errorsAsString() {
		if(logItems.isEmpty()) {
			return "[EMPTY]";
		}
		StringBuilder sb = new StringBuilder();
		for(LogItem item : logItems) {
			if(item.level == LogLevel.ERROR)
				sb.append(item.toString()).append("\n");
		}	
		return sb.toString();
	}

	public static String asString() {
		if(logItems.isEmpty()) {
			return "[EMPTY]";
		}
		StringBuilder sb = new StringBuilder();
		for(LogItem item : logItems) {
			sb.append(item.toString()).append("\n");
		}	
		return sb.toString();
	}

	public static int size() {
		return logItems.size();
	}

	static PrintStream originalOut = System.out;
	static ByteArrayOutputStream baos;
	
    public static void startCapture() {
        baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        originalOut = System.out;
        System.setOut(ps);
    }
    
    public static String endCapture() throws IOException {
		System.setOut(originalOut);
    	baos.close();
		System.setOut(originalOut);
        return baos.toString();
	}
    
    public static String generateOnesComplementTable() {
		StringBuilder sb = new StringBuilder();
		var itt = OnesComplementNumber.iterator(-255,255,1);
		while(itt.hasNext()) {
			var num = itt.next();
			sb.append(num.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

}
