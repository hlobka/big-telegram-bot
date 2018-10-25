package joke;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import http.GetExecuter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JokesProvider {

    private final String site;
    private final String name;

    public JokesProvider(String site, String name) {
        this.site = site;
        this.name = name;
    }

    public static void main(String[] args) {
        new JokesProvider().provideNextUniqueJoke(100);
    }

    public JokesProvider() {
        this.site = "bash.im";
        this.name = "bash";
    }

    public String provideNextUniqueJoke(int limit) {
        ArrayList<Integer> usedJokes = loadUsedJokes();
        List<String> jokes = getJokes(limit);
        Collections.reverse(jokes);
        for (String joke : jokes) {
            int hashCode = joke.hashCode();
            if (!usedJokes.contains(hashCode)) {
                usedJokes.add(hashCode);
                saveUsedJokes(usedJokes);
                return joke;
            }
        }
        return "no jokes :(";
    }

    private List<String> getJokes(int limit) {
        List<String> jokes = new ArrayList<>();
        JsonArray jsonJokesArray;
        try {
            jsonJokesArray = GetExecuter.getAsJsonArray(JokesUrlProvider.getURL(site, name, limit));
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(this, e);
            return jokes;
        }
        jokes = IntStream.range(0, jsonJokesArray.size())
            .mapToObj(i -> getJokeFromJson(jsonJokesArray.get(i)))
            .collect(Collectors.toList());
        return jokes;
    }

    private String getJokeFromJson(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get("elementPureHtml").getAsString();
    }

    private void saveUsedJokes(ArrayList<Integer> usedJokes) {
        SharedObject.save(this, "usedJokes", usedJokes);
    }

    private ArrayList<Integer> loadUsedJokes() {
        return SharedObject.load(this, "usedJokes", ArrayList.class, new ArrayList<Integer>());
    }
}
