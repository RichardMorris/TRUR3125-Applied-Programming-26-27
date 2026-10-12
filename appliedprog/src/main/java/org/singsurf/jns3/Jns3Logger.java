package org.singsurf.jns3;
import java.util.ArrayList;
import java.util.List;
public class Jns3Logger {
    public enum LogLevel {
        RECEIVED, // Frame was received successfully
        IGNORED, // Frame was ignored 
        ERROR, SENT
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
    
    static public List<LogItem> getLogItems() {
        return logItems;
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

	public static String asString() {
		StringBuilder sb = new StringBuilder();
		for(LogItem item : logItems) {
			sb.append(item.toString()).append("\n");
		}	
		return sb.toString();
	}
    
}
