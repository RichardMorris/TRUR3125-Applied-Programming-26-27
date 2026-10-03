package org.singsurf.jns3;
import java.util.ArrayList;
import java.util.List;
public class Jns3Logger {
    public enum LogLevel {
        RECEIVED, // Frame was received successfully
        IGNORED, // Frame was ignored 
        ERROR
    }
    public static class LogItem {
        public String deviceName;
        public LogLevel level;
        public String message;

        public LogItem(String deviceName, LogLevel level, String message) {
            this.deviceName = deviceName;
            this.level = level;
            this.message = message;
        }

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
    
}
