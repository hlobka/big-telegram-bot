package atlassian.jira;

import atlassian.jira.subclient.VersionDto;
import atlassian.jira.subclient.VersionRestClientV2;
import com.atlassian.httpclient.apache.httpcomponents.ApacheAsyncHttpClient;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.*;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Lists;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import telegram.bot.data.LoginData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Nice example
 * https://github.com/eugenp/tutorials/blob/master/saas/src/main/java/com/baeldung/saas/jira/MyJiraClient.java
 */
public class JiraHelper {

    private final JiraRestClient client;
    private final Boolean useCache;
    private Map<String, Issue> cacheOfIssues;
    private Consumer<RestClientException> errorHandler;

    private JiraHelper(JiraRestClient client, Boolean useCache) {
        this.client = client;
        this.useCache = useCache;
        cacheOfIssues = new HashMap<>();
    }

    public static JiraHelper tryToGetClient(LoginData loginData, Boolean useCache, Consumer<RestClientException> errorHandler) {
        return tryToGetClient(loginData, useCache, errorHandler, new AsynchronousJiraRestClientFactory());
    }

    public static JiraHelper tryToGetClient(LoginData loginData, Boolean useCache, Consumer<RestClientException> errorHandler, JiraRestClientFactory factory) {
        while (true) {
            try {
                URI uri = new URI(loginData.url);
                JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass);
                JiraHelper clientHelper = getClient(client, useCache);
                clientHelper.useErrorHandler(errorHandler);
                return clientHelper;
            } catch (RestClientException e) {
                ConsoleLogger.logErrorFor(JiraHelper.class, e);
                errorHandler.accept(e);
            } catch (URISyntaxException e) {
                ConsoleLogger.logErrorFor(JiraHelper.class, e);
                throw new RuntimeException(e);
            }
        }
    }

    private void useErrorHandler(Consumer<RestClientException> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static JiraHelper getClient(LoginData loginData) {
        return getClient(loginData, false);
    }

    public static JiraHelper getClient(LoginData loginData, Boolean useCache) {
        try {
            URI uri = new URI(loginData.url);
            JiraRestClient client = getJiraRestClient(uri, loginData.login, loginData.pass);
            return getClient(client, useCache);
        } catch (URISyntaxException ex) {
            ConsoleLogger.logErrorFor(JiraHelper.class, ex);
            throw new RuntimeException(ex);
        }
    }

    public static AsynchronousJiraRestClient getJiraRestClient(URI serverUri, String username, String password) {
        AuthenticationHandler authenticationHandler = (new BasicHttpAuthenticationHandler(username, password));
        DisposableHttpClient httpClient = (new AsynchronousHttpClientFactory()).createClient(serverUri, authenticationHandler);
        AtlassianHttpClientDecorator atlassianHttpClientDecorator = (AtlassianHttpClientDecorator) httpClient;
        try {
            ApacheAsyncHttpClient httpClient1 = (ApacheAsyncHttpClient) FieldUtils.readField(atlassianHttpClientDecorator, "httpClient", true);
            ((HttpClientOptions) FieldUtils.readField(httpClient1, "httpClientOptions", true)).setSocketTimeout(2, TimeUnit.MINUTES);
        } catch (IllegalAccessException e) {
            ConsoleLogger.logErrorFor(JiraHelper.class, e);
        }
        return new AsynchronousJiraRestClient(serverUri, httpClient);
    }

    public static JiraHelper getClient(JiraRestClient client, Boolean useCache) {
        return new JiraHelper(client, useCache);
    }

    public void resetCache() {
        cacheOfIssues = new HashMap<>();
    }

    public Boolean hasIssue(String issueKey) {
        if (useCache && cacheOfIssues.containsKey(issueKey)) {
            return true;
        }
        try {
            return getIssue(issueKey, false) != null;
        } catch (RestClientException e) {
            for (ErrorCollection errorCollection : e.getErrorCollections()) {
                if (errorCollection.getErrorMessages().contains("Issue Does Not Exist")) {
                    return false;
                }
            }
            return getIssue(issueKey, true) != null;
        }
    }

    public SprintDto getActiveSprint(String projectId) {
        String jql = FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectId);
        List<Issue> issues = getIssues(jql, 1, 0);
        if (issues.isEmpty()) {
            throw new RuntimeException(String.format("No active sprints for project: %s", projectId));
        }
        IssueField sprintIssueField = issues.get(0).getFieldByName("Sprint");
        if (sprintIssueField == null) {
            throw new RuntimeException(String.format("Sprint issues without Sprint Field in project: %s", projectId));
        }
        Object sprintValue = sprintIssueField.getValue();
        List<String> sprintValues;
        if (sprintValue instanceof JSONArray) {
            sprintValues = new ArrayList<>();
            JSONArray sprintValuesJsArray = (JSONArray) sprintValue;
            for (int i = 0; i < sprintValuesJsArray.length(); i++) {
                try {
                    sprintValues.add(sprintValuesJsArray.getString(i));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        } else if (sprintValue instanceof List) {
            sprintValues = (List<String>) sprintValue;
        } else {
            throw new RuntimeException(String.format("No sprint values for project: %s", projectId));
        }
        for (String value : Lists.reverse(sprintValues)) {
            Function<String, Date> dateProvider = s -> {
                if (s == null || "<null>".equals(s)) {
                    return null;
                }
                s = s.replaceAll("T", " ");
                try {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(s);
                } catch (ParseException e) {
                    throw new RuntimeException(String.format("Could not parse: %s", s), e);
                }
            };
            SprintDto sprint = new SprintDto(
                Long.parseLong(StringHelper.getRegString(value, ".*id=(.+?)(?=,).*", 1, 0)),
                Long.parseLong(StringHelper.getRegString(value, ".*rapidViewId=(.+?)(?=,).*", 1, 0)),
                StringHelper.getRegString(value, ".*state=(.+?)(?=,).*", 1, 0),
                StringHelper.getRegString(value, ".*name=(.+?)(?=,).*", 1, 0),
                StringHelper.getRegString(value, ".*startDate=(.+?)(?=,).*", 1, 0),
                dateProvider.apply(StringHelper.getRegString(value, ".*startDate=(.+?)(?=,).*", 1, 0)),
                StringHelper.getRegString(value, ".*endDate=(.+?)(?=,).*", 1, 0),
                dateProvider.apply(StringHelper.getRegString(value, ".*endDate=(.+?)(?=,).*", 1, 0)),
                StringHelper.getRegString(value, ".*completeDate=(.+?)(?=,).*", 1, 0),
                dateProvider.apply(StringHelper.getRegString(value, ".*completeDate=(.+?)(?=,).*", 1, 0)),
                Long.parseLong(StringHelper.getRegString(value, ".*sequence=(.+?)(?=,).*", 1, 0)),
                StringHelper.getRegString(value, ".*goal=(.+?)(?=]).*", 1, 0)
            );
            if (sprint.getState().equalsIgnoreCase("ACTIVE")) {
                return sprint;
            }
        }
        throw new RuntimeException(String.format("Active sprint where not found by projectId: %s", projectId));
    }

    public VersionDto getVersion(URI uri) {
        try {
            HttpClient httpClient = (HttpClient) FieldUtils.readField(client, "httpClient", true);
            VersionRestClientV2 versionRestClient = new VersionRestClientV2(httpClient);
            return versionRestClient.getVersion(uri);
        } catch (IllegalAccessException e) {
            ConsoleLogger.logError(e, "getVersion: " + uri);
        }
        return null;
    }

    public Project getProject(String projectId) {
        Project result = client.getProjectClient().getProject(projectId).claim();
        return result;
    }

    public List<Issue> getIssues(String jql) {
        return getIssues(jql, false);
    }

    public List<Issue> getIssues(String jql, boolean fillInformation) {
        return getIssues(jql, 100, 0, fillInformation);
    }

    public List<Issue> getIssues(String jql, Integer maxPerQuery, Integer startIndex) {
        return getIssues(jql, maxPerQuery, startIndex, false);
    }

    public List<Issue> getIssues(String jql, Integer maxPerQuery, Integer startIndex, boolean fillInformation) {
        return getIssues(jql, new JqlCriteria()
            .withMaxPerQuery(maxPerQuery)
            .withStartIndex(startIndex)
            .withFields(null)
            .withFillInformation(fillInformation)
        );
    }

    public List<Issue> getIssues(String jql, JqlCriteria jqlCriteria) {
        Integer maxPerQuery = jqlCriteria.maxPerQuery;
        Integer startIndex = jqlCriteria.startIndex;
        Set<String> fields = jqlCriteria.fields;

        SearchRestClient searchRestClient = client.getSearchClient();
        Promise<SearchResult> searchResult = searchRestClient.searchJql(jql, maxPerQuery, startIndex, fields);
        SearchResult results = searchResult.claim();
        ArrayList<Issue> result = new ArrayList<>();
        results.getIssues().forEach(result::add);
        return result;
    }

    public Issue getIssue(String issueKey) {
        return getIssue(issueKey, true);
    }

    public Issue getIssue(String issueKey, boolean checkRelogin) {
        if (useCache && cacheOfIssues.containsKey(issueKey)) {
            return cacheOfIssues.get(issueKey);
        }
        Promise<Issue> promise = client.getIssueClient().getIssue(issueKey);
        Issue issue;
        try {
            issue = promise.claim();
        } catch (RestClientException e) {
            if (errorHandler != null && checkRelogin) {
                errorHandler.accept(e);
                promise = client.getIssueClient().getIssue(issueKey);
                issue = promise.claim();
            } else {
                throw e;
            }
        }
        if (useCache) {
            cacheOfIssues.put(issueKey, issue);
        }
        return issue;
    }

    public void assignIssueOn(String issueKey, String userLoginName) throws RestClientException {
        ComplexIssueInputFieldValue value = ComplexIssueInputFieldValue.with("name", userLoginName);
        FieldInput fieldInput = new FieldInput("assignee", value);
        IssueInput issueInput = IssueInput.createWithFields(fieldInput);
        client.getIssueClient().updateIssue(issueKey, issueInput).claim();
    }

    public void addIssueComment(String issueKey, String commentValue) {
        URI commentsUri = getIssue(issueKey).getCommentsUri();
        Comment comment = Comment.valueOf(commentValue);
        client.getIssueClient().addComment(commentsUri, comment).claim();
    }

    public void disconnect() {
        try {
            client.close();
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(this, e);
            e.printStackTrace();
        }
    }
}
