package helper.logger;

import helper.time.TimeHelper;

/**
 * Just to get logs with date;
 * TODO: replace with normal logger;
 */
public class ConsoleLogger {
    public static ILogger additionalLogger = message -> {
    };
    public static ILogger additionalErrorLogger = message -> {
    };

    public static void logFor(Object o, String message) {
        logFor(o.getClass(), message);
    }

    public static void logFor(Class<?> clazz, String message) {
        log(clazz.getSimpleName() + "::" + message);
    }

    public static void log(String message) {
        String messageToBeLog = TimeHelper.getCurrentTimeStamp("HH:mm:ss") + ": " + message;
        System.out.println(messageToBeLog);
        additionalLogger.log(messageToBeLog);
    }

    public static void logErrorFor(Object o, Throwable throwable) {
        logErrorFor(o.getClass(), throwable);
    }

    public static void logErrorFor(Class<?> clazz, Throwable throwable) {
        logError(throwable, clazz.getSimpleName() + "::" + throwable.getClass().getSimpleName());
    }

    public static void logError(Throwable throwable, String caption) {
        String messageToBeLog = TimeHelper.getCurrentTimeStamp("HH:mm:ss") + ": " + caption + ":\n" + throwable.getMessage();
        System.err.println(messageToBeLog);
        additionalErrorLogger.log(messageToBeLog);
        throwable.printStackTrace();
    }
}
