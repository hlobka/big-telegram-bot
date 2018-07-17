package atlassian.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import telegram.bot.data.LoginData;

import java.net.URI;
import java.net.URISyntaxException;

public class JiraHelper {

    private final JiraRestClient client;

    private JiraHelper(LoginData loginData) {
        try {
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            URI uri = new URI(loginData.url);
            client = factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static JiraHelper getClient(LoginData loginData) {
        return new JiraHelper(loginData);
    }

    public Issue getIssue(String issueKey) {
        Promise<Issue> promise = client.getIssueClient().getIssue(issueKey);
        return promise.claim();
    }
}
