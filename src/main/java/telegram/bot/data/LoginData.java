package telegram.bot.data;

import helper.string.StringHelper;

import java.util.Properties;

public class LoginData {
    public final String url;
    public final String login;
    public final String pass;

    public LoginData(String url, String login, String pass) {
        this.url = url;
        this.login = login;
        this.pass = pass;
    }

    public LoginData(Properties properties, final String type) {
        url = properties.getProperty(type + ".url");
        login = properties.getProperty(type + ".auth.login");
        if(properties.containsKey(type + ".auth.cryptedPass") && !"".equals(properties.get(type + ".auth.cryptedPass"))){
            pass = StringHelper.getAsSimpleDeCrypted(properties.getProperty(type + ".auth.cryptedPass"));
        } else {
            pass = properties.getProperty(type + ".auth.pass");
        }
    }
}
