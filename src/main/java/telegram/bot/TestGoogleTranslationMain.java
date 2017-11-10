package telegram.bot;

import com.google.gson.JsonObject;
import http.GetExecuter;
import telegram.bot.data.Common;

import java.io.IOException;

public class TestGoogleTranslationMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        JsonObject response = GetExecuter.getAsJson(Common.GOOGLE.getUrl("SPAM"));
        String firstResult = response.get("items").getAsJsonArray().get(0).getAsJsonObject().get("snippet").getAsString();
        System.out.println("response: " + response.toString() + "");
        System.out.println("first result: " + firstResult + "");

    }
}
