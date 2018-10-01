package helper.file;

import helper.logger.ConsoleLogger;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SharedObject {
    public static <K, V> HashMap<K, V> loadMap(String url, HashMap<K, V> defaultValue) {
        HashMap<K, V> result;
        if (createNewFile(url)) {
            save(url, defaultValue);
        }
        try (FileInputStream fileIn = new FileInputStream(url); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            result = (HashMap<K, V>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ConsoleLogger.logErrorFor(SharedObject.class, e);
            return defaultValue;
        }
        return result;
    }

    public static HashMap<String, String> loadMap(String url) {
        return loadMap(url, new HashMap<String, String>());
    }

    private static boolean checkIsExist(String url) {
        return new File(url).exists();
    }

    private static boolean createNewFile(String url) {
        String folders = url.replaceAll("\\/[a-zA-Z0-9]+\\.\\w+", "");
        if (!folders.contains(".")) {
            new File(folders).mkdirs();
        }
        if (new File(url).exists()) {
            return false;
        }
        try {
            return new File(url).createNewFile();
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(SharedObject.class, e);
            return true;
        }
    }

    public static void save(String url, Serializable data) {
        createNewFile(url);
        try (FileOutputStream fileOut = new FileOutputStream(url); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(data);
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(SharedObject.class, e);
        } finally {
            System.out.println("Serialized data is saved in " + url);
        }
    }

    private static void removeFile(String url) {
        if (new File(url).exists()) {
            if(!new File(url).delete()){
                try {
                    FileUtils.deleteDirectory(new File(url));
                } catch (IOException e) {
                    ConsoleLogger.logErrorFor(SharedObject.class, e);
                }
            }
        }
    }

    public static void remove(Object instance, String sharedObjectName) {
        String classUrl = getClassUrl(instance.getClass(), sharedObjectName);
        remove(classUrl);
    }

    public static void remove(String url) {
        removeFile(url);
    }

    public static <T> T load(String url, Class<T> eClass) {
        return loadOrDefault(url, eClass, null);
    }

    public static <T> T loadOrDefault(String url, Class<T> eClass, T defaultValue) {
        T result;
        if (!checkIsExist(url)) {
            return defaultValue;
        }
        createNewFile(url);
        try (FileInputStream fileIn = new FileInputStream(url); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            result = (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ConsoleLogger.logErrorFor(SharedObject.class, e);
            return defaultValue;
        }
        return result;
    }

    public static ArrayList<String> loadList(String url) {
        return loadList(url, new ArrayList<>());
    }

    public static <E> ArrayList<E> loadList(String url, ArrayList<E> defaultValue) {
        ArrayList result = load(url, ArrayList.class);
        return result == null ? defaultValue : result;
    }

    public static void save(Object instance, String sharedObjectName, Serializable data) {
        save(instance.getClass(), sharedObjectName, data);
    }

    public static void save(Class<?> instanceClass, String sharedObjectName, Serializable data) {
        String url = getClassUrl(instanceClass, sharedObjectName);
        save(url, data);
    }

    public static <T> T load(Object instance, String sharedObjectName, Class<T> eClass, T defaultValue) {
        return load(instance.getClass(), sharedObjectName, eClass, defaultValue);
    }

    public static <T> T load(Object instance, String sharedObjectName, Class<T> eClass) {
        return load(instance.getClass(), sharedObjectName, eClass);
    }

    public static <T> T load(Class<?> instanceClass, String sharedObjectName, Class<T> eClass) {
        return load(instanceClass, sharedObjectName, eClass, null);
    }

    public static <T> T load(Class<?> instanceClass, String sharedObjectName, Class<T> eClass, T defaultValue) {
        String url = getClassUrl(instanceClass, sharedObjectName);
        return loadOrDefault(url, eClass, defaultValue);
    }

    /**
     * @param defaultValue should not be null
     * @throws NullPointerException when default value is null
     */
    public static <T> T load(Object instance, String sharedObjectName, T defaultValue) {
        return load(instance.getClass(), sharedObjectName, defaultValue);
    }

    /**
     * @param defaultValue should not be null
     * @throws NullPointerException when default value is null
     */
    public static <T> T load(Class<?> instanceClass, String sharedObjectName, T defaultValue) {
        String url = getClassUrl(instanceClass, sharedObjectName);
        if(defaultValue == null) {
            throw new NullPointerException("default value should be not null");
        }
        Class<T> aClass = (Class<T>) defaultValue.getClass();
        return loadOrDefault(url, aClass, defaultValue);
    }

    public static String getClassUrl(Class<?> instanceClass, String sharedObjectName) {
        String simpleName = instanceClass.getSimpleName();
        return "/tmp/" + simpleName + "/" + sharedObjectName + ".ser";
    }
}
