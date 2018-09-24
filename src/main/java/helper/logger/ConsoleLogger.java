package helper.logger;

import helper.time.TimeHelper;

/**
 * Just to get logs with date;
 * TODO: replace with normal logger;
 */
public class ConsoleLogger {
    public static void logFor(Object o, String message) {
        log(o.getClass() + "::" + message);
    }
    public static void logFor(Class<?> clazz, String message) {
        log(clazz.getSimpleName() + "::" + message);
    }
    public static void log(String message) {
        System.out.println(TimeHelper.getCurrentTimeStamp("HH:mm:ss") + ": " + message);
    }
}
