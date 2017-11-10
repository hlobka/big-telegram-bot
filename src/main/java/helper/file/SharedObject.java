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

    private static boolean createNewFile(String url) {
        String[] strings = url.split("/");
        String filePath = "";
        for (String fileName : strings) {
            System.out.println("fileName = [" + fileName + "]");
            if (fileName.isEmpty()) {
                continue;
            }
            filePath += "/" + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                continue;
            }
            if (fileName.contains(".")) {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                file.mkdir();
            }
        }
        return false;
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
}
