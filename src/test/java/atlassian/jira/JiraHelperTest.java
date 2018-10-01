package atlassian.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import telegram.bot.data.LoginData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import static org.testng.Assert.*;

public class JiraHelperTest {

    @Test
    public void testResetCache() {
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        IssueRestClient issueRestClientMock = Mockito.mock(IssueRestClient.class);
        Promise<Issue> issuePromiseMock = Mockito.mock(Promise.class);
        Issue issueMock = Mockito.mock(Issue.class);
        Mockito.when(jiraRestClientMock.getIssueClient()).thenReturn(issueRestClientMock);
        Mockito.when(issueRestClientMock.getIssue("testKey")).thenReturn(issuePromiseMock);
        Mockito.when(issuePromiseMock.claim()).thenReturn(issueMock);

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, true);

        helper.getIssue("testKey");
        helper.getIssue("testKey");
        helper.resetCache();
        helper.getIssue("testKey");

        Mockito.verify(jiraRestClientMock, Mockito.times(2)).getIssueClient();
        Mockito.verify(issueRestClientMock, Mockito.times(2)).getIssue("testKey");
        Mockito.verify(issuePromiseMock, Mockito.times(2)).claim();
    }

    @Test
    public void testGetIssue() {
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        IssueRestClient issueRestClientMock = Mockito.mock(IssueRestClient.class);
        Promise<Issue> issuePromiseMock = Mockito.mock(Promise.class);
        Issue issueMock = Mockito.mock(Issue.class);
        Mockito.when(jiraRestClientMock.getIssueClient()).thenReturn(issueRestClientMock);
        Mockito.when(issueRestClientMock.getIssue("testKey")).thenReturn(issuePromiseMock);
        Mockito.when(issuePromiseMock.claim()).thenReturn(issueMock);

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, false);

        Assertions.assertThat(helper).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();

        Mockito.verify(jiraRestClientMock).getIssueClient();
        Mockito.verify(issueRestClientMock).getIssue("testKey");
        Mockito.verify(issuePromiseMock).claim();
    }

    @Test
    public void testGetIssueWithoutCache() {
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        IssueRestClient issueRestClientMock = Mockito.mock(IssueRestClient.class);
        Promise<Issue> issuePromiseMock = Mockito.mock(Promise.class);
        Issue issueMock = Mockito.mock(Issue.class);
        Mockito.when(jiraRestClientMock.getIssueClient()).thenReturn(issueRestClientMock);
        Mockito.when(issueRestClientMock.getIssue("testKey")).thenReturn(issuePromiseMock);
        Mockito.when(issuePromiseMock.claim()).thenReturn(issueMock);

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, false);

        Assertions.assertThat(helper).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();

        Mockito.verify(jiraRestClientMock, Mockito.times(2)).getIssueClient();
        Mockito.verify(issueRestClientMock, Mockito.times(2)).getIssue("testKey");
        Mockito.verify(issuePromiseMock, Mockito.times(2)).claim();
    }

    @Test
    public void testGetIssueWithCache() {
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        IssueRestClient issueRestClientMock = Mockito.mock(IssueRestClient.class);
        Promise<Issue> issuePromiseMock = Mockito.mock(Promise.class);
        Issue issueMock = Mockito.mock(Issue.class);
        Mockito.when(jiraRestClientMock.getIssueClient()).thenReturn(issueRestClientMock);
        Mockito.when(issueRestClientMock.getIssue("testKey")).thenReturn(issuePromiseMock);
        Mockito.when(issuePromiseMock.claim()).thenReturn(issueMock);

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, true);

        Assertions.assertThat(helper).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();
        Assertions.assertThat(helper.getIssue("testKey")).isNotNull();

        Mockito.verify(jiraRestClientMock, Mockito.times(1)).getIssueClient();
        Mockito.verify(issueRestClientMock, Mockito.times(1)).getIssue("testKey");
        Mockito.verify(issuePromiseMock, Mockito.times(1)).claim();

    }

    @Test
    public void tryToGetClient() {
        LoginData loginData = new LoginData("testUrl", "testLogin", "testPass");
        Consumer<RestClientException> errorHandler = Mockito.mock(Consumer.class);
        JiraRestClientFactory factory = Mockito.mock(JiraRestClientFactory.class);
        JiraHelper.tryToGetClient(loginData, false, errorHandler, factory);
    }

    @Test
    public void tryToGetClientWithErrorHandling() throws URISyntaxException {
        LoginData loginData = new LoginData("testUrl", "testLogin", "testPass");

        JiraRestClientFactory factory = Mockito.mock(JiraRestClientFactory.class);
        URI uri = new URI(loginData.url);
        Mockito.when(factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass))
            .thenThrow(RestClientException.class);
        JiraRestClient client = Mockito.mock(JiraRestClient.class);
        Consumer<RestClientException> errorHandler = e -> {
            Mockito.reset(factory);
            Mockito.when(factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass))
                .thenReturn(client);
        };
        JiraHelper.tryToGetClient(loginData, false, errorHandler, factory);
    }

    @Test
    public void testHasIssue() {
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        IssueRestClient issueRestClientMock = Mockito.mock(IssueRestClient.class);
        Promise<Issue> issuePromiseMock = Mockito.mock(Promise.class);
        Issue issueMock = Mockito.mock(Issue.class);
        Mockito.when(jiraRestClientMock.getIssueClient()).thenReturn(issueRestClientMock);
        Mockito.when(issueRestClientMock.getIssue("testKey")).thenReturn(issuePromiseMock);
        Mockito.when(issuePromiseMock.claim()).thenReturn(issueMock);

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, false);

        Assertions.assertThat(helper).isNotNull();
        Assertions.assertThat(helper.hasIssue("testKey")).isTrue();

        Mockito.verify(jiraRestClientMock).getIssueClient();
        Mockito.verify(issueRestClientMock).getIssue("testKey");
        Mockito.verify(issuePromiseMock).claim();
    }
}