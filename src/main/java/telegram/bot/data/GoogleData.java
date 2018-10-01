package telegram.bot.data;

import com.google.gson.JsonObject;
import helper.logger.ConsoleLogger;
import http.GetExecuter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

public class GoogleData {
    public static final String TEMPLATE = "https://www.googleapis.com/customsearch/v1?q={searchTerms}&num={count?}&start={startIndex?}&lr={language?}&safe={safe?}&cx={cx?}&sort={sort?}&filter={filter?}&gl={gl?}&cr={cr?}&googlehost={googleHost?}&c2coff={disableCnTwTranslation?}&hq={hq?}&hl={hl?}&siteSearch={siteSearch?}&siteSearchFilter={siteSearchFilter?}&exactTerms={exactTerms?}&excludeTerms={excludeTerms?}&linkSite={linkSite?}&orTerms={orTerms?}&relatedSite={relatedSite?}&dateRestrict={dateRestrict?}&lowRange={lowRange?}&highRange={highRange?}&searchType={searchType}&fileType={fileType?}&rights={rights?}&imgSize={imgSize?}&imgType={imgType?}&imgColorType={imgColorType?}&imgDominantColor={imgDominantColor?}&alt=json";
    private static final String QUERY = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s";
    private final String cx;
    private final String key;

    GoogleData(Properties properties) {
        cx = properties.getProperty("google.api.translations.CX");
        key = properties.getProperty("google.api.translations.KEY");
    }

    public String getUrl(String query) {
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ConsoleLogger.logErrorFor(this, e);
            return String.format(QUERY, key, cx, query);
        }
        return String.format(QUERY, key, cx, encodedQuery);
    }

    public String getFirstResult(String query) {
        JsonObject response;
        try {
            response = GetExecuter.getAsJson(getUrl(query));
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(this, e);
            try {
                return "https://www.google.com.ua/search?q=" + URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e2) {
                ConsoleLogger.logErrorFor(this, e);
                return "Попробуй поискать в гугле https://www.google.com.ua/search?q=" + query;
            }
        }
        int totalResults = response.get("searchInformation").getAsJsonObject().get("totalResults").getAsInt();
        if (totalResults > 0) {
            JsonObject firstItem = response.get("items").getAsJsonArray().get(0).getAsJsonObject();
            String result = firstItem.get("snippet").getAsString();
            if (firstItem.has("formattedUrl")) {
                result += "\n[" + query + "](" + firstItem.get("formattedUrl").getAsString() + ")";
            }
            return result;
        } else {
            return "Вики ничего не знает о этом. \nИщите в гугле: \nhttps://www.google.com.ua/search?q=" + query;
        }
    }
}
