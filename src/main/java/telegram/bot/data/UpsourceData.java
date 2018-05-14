package telegram.bot.data;

import java.util.Properties;

public class UpsourceData {
    public final String url;
    public final String login;
    public final String pass;

    public UpsourceData(Properties properties) {
        url = properties.getProperty("upsource.url");
        login = properties.getProperty("upsource.auth.login");
        pass = properties.getProperty("upsource.auth.pass");
    }
}
