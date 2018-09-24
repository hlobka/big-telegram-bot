package atlassian.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import telegram.bot.data.LoginData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JiraHelper {

    private final JiraRestClient client;
    private final Boolean useCache;
    private Map<String, Issue> cacheOfIssues;

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
                return clientHelper;
            } catch (RestClientException e) {
                e.printStackTrace();
                errorHandler.accept(e);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static JiraHelper getClient(LoginData loginData) {
        return getClient(loginData, false);
    }

    public static JiraHelper getClient(LoginData loginData, Boolean useCache) {
        try {
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            URI uri = new URI(loginData.url);
            JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass);
            return getClient(client, useCache);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
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
            return getIssue(issueKey) != null;
        } catch (RestClientException e) {
            return false;
        }
    }

    public Issue getIssue(String issueKey) {
        if (useCache && cacheOfIssues.containsKey(issueKey)) {
            return cacheOfIssues.get(issueKey);
        }
        Promise<Issue> promise = client.getIssueClient().getIssue(issueKey);
        Issue issue = promise.claim();
        if (useCache) {
            cacheOfIssues.put(issueKey, issue);
        }
        return issue;
    }
}
