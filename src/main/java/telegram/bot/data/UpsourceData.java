package telegram.bot.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UpsourceData {
    public final String url;
    public final String login;
    public final String pass;
    public final Map<String, String> userIdOnNameMap;
    public final Map<String, String> userLoginOnMailMap;
    public String checkerHelpLink;
    public String checkerPossibleProblemsHelpLink;

    public UpsourceData(Properties properties) {
        checkerHelpLink = properties.getProperty("upsource.checkerHelpLink");
        checkerPossibleProblemsHelpLink = properties.getProperty("upsource.checkerPossibleProblemsHelpLink");
        LoginData loginData = new LoginData(properties, "upsource");
        url = loginData.url;
        login = loginData.login;
        pass = loginData.pass;
        userIdOnNameMap = new HashMap<>();
        userLoginOnMailMap = new HashMap<>();
        collectMapValues(properties, "upsource.userIdsMap", userIdOnNameMap);
        collectMapValues(properties, "upsource.userLoginOnMailMap", userLoginOnMailMap);

    }

    public void collectMapValues(Properties properties, String key, Map<String, String> map) {
        String userIdsMap = properties.getProperty(key);
        for (String userIdsOnName : userIdsMap.split(",")) {
            String[] values = userIdsOnName.split(":");
            map.put(values[0], values[1]);
        }
    }
}
