package atlassian.jira.subclient;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import java.net.URI;

public class VersionJsonParserV2 implements JsonObjectParser<VersionDto> {
    @Override
    public VersionDto parse(JSONObject json) throws JSONException {
        URI self = JsonParseUtil.getSelfUri(json);
        Long id = JsonParseUtil.getOptionalLong(json, "id");
        String name = json.getString("name");
        String description = JsonParseUtil.getOptionalString(json, "description");
        boolean isArchived = json.getBoolean("archived");
        boolean isReleased = json.getBoolean("released");
        String startDateStr = JsonParseUtil.getOptionalString(json, "startDate");
        DateTime startDate = this.parseReleaseDate(startDateStr);
        String releaseDateStr = JsonParseUtil.getOptionalString(json, "releaseDate");
        DateTime releaseDate = this.parseReleaseDate(releaseDateStr);
        return new VersionDto(self, id, name, description, isArchived, isReleased, startDate, releaseDate);
    }

    private DateTime parseReleaseDate(String releaseDateStr) {
        if (releaseDateStr != null) {
            return releaseDateStr.length() > "YYYY-MM-RR".length() ? JsonParseUtil.parseDateTime(releaseDateStr) : JsonParseUtil.parseDate(releaseDateStr);
        } else {
            return null;
        }
    }
}
