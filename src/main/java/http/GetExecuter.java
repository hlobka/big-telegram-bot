package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public static JsonArray getAsJsonArray(String url) throws IOException {
        return getAsJsonElement(url).getAsJsonArray();
    }

    public static JsonObject getAsJson(String url) throws IOException {
        JsonElement jsonElement = getAsJsonElement(url);
        return jsonElement.getAsJsonObject();
    }

    public static JsonElement getAsJsonElement(String url) throws IOException {
        String response = execute(url);
        return new JsonParser().parse(response);
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
