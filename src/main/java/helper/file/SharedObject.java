package helper.file;

import java.io.*;
import java.util.HashMap;

public class SharedObject {
    public static <K, V> HashMap<K, V> loadMap(String url, HashMap<K, V> defaultValue) {
        HashMap<K, V> result;
        if(createNewFile(url)){
            save(url, defaultValue);
        }
        try (FileInputStream fileIn = new FileInputStream(url); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            result = (HashMap<K, V>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
        if(!folders.contains(".")) {
            new File(folders).mkdirs();
        }
        if(new File(url).exists()){
            return false;
        }
        try {
            return new File(url).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void save(String url, Serializable data) {
        createNewFile(url);
        try (FileOutputStream fileOut = new FileOutputStream(url);ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Serialized data is saved in " + url);
        }
    }

    private static void removeFile(String url) {
        if(new File(url).exists()){
            new File(url).delete();
        }
    }

    public static void remove(String url) {
        removeFile(url);
    }

    public static <T> T load(String url, Class<T> eClass) {
        T result;
        if(!checkIsExist(url)){
            return null;
        }
        createNewFile(url);
        try (FileInputStream fileIn = new FileInputStream(url); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            result = (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
