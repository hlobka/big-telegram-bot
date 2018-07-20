package atlassian.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;

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
}