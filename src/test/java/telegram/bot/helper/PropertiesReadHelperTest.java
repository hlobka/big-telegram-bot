package telegram.bot.helper;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Properties;

public class PropertiesReadHelperTest {

    @Test
    public void testGetMapProperties() {
        Properties properties = new Properties();
        properties.setProperty("someting", "key1:1,key2:2");
        PropertiesReadHelper testTarget = new PropertiesReadHelper(properties);
        Map<String, Integer> map = testTarget.getStringIntMap("someting");
        Assertions.assertThat(map).containsKeys("key1", "key2");
        Assertions.assertThat(map.get("key1")).isEqualTo(1);
        Assertions.assertThat(map.get("key2")).isEqualTo(2);
        Assertions.assertThat(map.get("key3")).isNull();
    }
}