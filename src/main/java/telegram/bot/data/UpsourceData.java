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
    public String checkerHelpLink;

    public UpsourceData(Properties properties) {
        checkerHelpLink = properties.getProperty("upsource.checkerHelpLink");
        LoginData loginData = new LoginData(properties, "upsource");
        url = loginData.url;
        login = loginData.login;
        pass = loginData.pass;
        userIdOnNameMap = new HashMap<>();
        String userIdsMap = properties.getProperty("upsource.userIdsMap");
        for (String userIdsOnName : userIdsMap.split(",")) {
            String[] values = userIdsOnName.split(":");
            userIdOnNameMap.put(values[0], values[1]);
        }

    }
}
