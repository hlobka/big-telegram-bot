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
import java.util.*;

public class JiraHelper {

    private final JiraRestClient client;
    private final Boolean useCache;
    private Map<String, Issue> cacheOfIssues;

    private JiraHelper(JiraRestClient client, Boolean useCache) {
        this.client = client;
        this.useCache = useCache;
        cacheOfIssues = new HashMap<>();
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
