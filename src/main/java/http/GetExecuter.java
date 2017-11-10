package http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GetExecuter {
    public static String execute(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        return convertStreamToString(is);
    }

    public static JsonObject getAsJson(String url) throws IOException {
        String response = execute(url);
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        return jsonObject;

    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
