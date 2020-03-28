package telegram.bot.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesReadHelper {

    private final Properties properties;

    public PropertiesReadHelper(Properties properties) {
        this.properties = properties;
    }

    public  Map<String, Integer> getStringIntMap(String key) {
        Map<String, Integer> result = new HashMap<>();
        try {
            String value = properties.getProperty(key);
            if(value.isEmpty()) {
                return result;
            }
            String[] entryValues = value.split(",");
            for (String entryValue : entryValues) {
                String[] split = entryValue.split(":");
                result.put(split[0], Integer.valueOf(split[1]));
            }
            return result;
        } catch (RuntimeException e){
            throw new RuntimeException(String.format("Could not read %s from properties file", key), e);
        }
    }
}
