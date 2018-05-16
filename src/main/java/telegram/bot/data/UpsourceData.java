package telegram.bot.data;

import helper.string.StringHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UpsourceData {
    public final String url;
    public final String login;
    public final String pass;
    public final Map<String, String> userIdOnNameMap;

    public UpsourceData(Properties properties) {
        url = properties.getProperty("upsource.url");
        login = properties.getProperty("upsource.auth.login");
        if(properties.containsKey("upsource.auth.cryptedPass")){
            pass = StringHelper.getAsSimpleDeCrypted(properties.getProperty("upsource.auth.cryptedPass"));
        } else {
            pass = properties.getProperty("upsource.auth.pass");
        }
        userIdOnNameMap = new HashMap<>();
        String userIdsMap = properties.getProperty("upsource.userIdsMap");
        for (String userIdsOnName : userIdsMap.split(",")) {
            String[] values = userIdsOnName.split(":");
            userIdOnNameMap.put(values[0], values[1]);
        }

    }
}
