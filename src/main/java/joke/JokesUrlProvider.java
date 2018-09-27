package joke;

public class JokesUrlProvider {
    private static final String URL = "http://umorili.herokuapp.com/api/get?site=%s&name=%s&num=%d";

    public static String getURL() {
        return getURL("bash.im", "bash", 1);
    }

    public static String getURL(int i) {
        return getURL("bash.im", "bash", i);
    }

    public static String getURL(String site, String name, int amount) {
        return String.format(URL, site, name, amount);
    }
}
