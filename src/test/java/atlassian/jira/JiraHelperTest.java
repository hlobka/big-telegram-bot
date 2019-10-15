package atlassian.jira;

import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import telegram.bot.data.LoginData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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

    @Test
    public void testGetSprint() {
        SearchRestClient mockedSearchRestClient = Mockito.mock(SearchRestClient.class);
        JiraRestClient jiraRestClientMock = Mockito.mock(JiraRestClient.class);
        Mockito.when(jiraRestClientMock.getSearchClient()).thenReturn(mockedSearchRestClient);
        String jql = FavoriteJqlScriptHelper.getSprintAllIssuesJql("TEST_ID");
        List<Issue> issues = Arrays.asList(Mockito.mock(Issue.class), Mockito.mock(Issue.class));
        Promise<SearchResult> issuesPromiseMock = Mockito.mock(Promise.class);
        SearchResult mockedSearchResult= Mockito.mock(SearchResult.class);
        IssueField mockedIssueField= Mockito.mock(IssueField.class);
        Mockito.when(issuesPromiseMock.claim()).thenReturn(mockedSearchResult);
        Mockito.when(mockedSearchResult.getIssues()).thenReturn(issues);
        Mockito.when(mockedSearchRestClient.searchJql(jql, 1, 0, null)).thenReturn(issuesPromiseMock);
        Mockito.when(issues.get(0).getFieldByName("Sprint")).thenReturn(mockedIssueField);
        Mockito.when(mockedIssueField.getValue()).thenReturn(Arrays.asList(
            "com.atlassian.greenhopper.service.sprint.Sprint@6d6b0049[id=6669,rapidViewId=5406,state=CLOSED,name=FOE Sprint 0,startDate=2019-03-05T12:58:20.120+01:00,endDate=2019-03-15T12:58:00.000+01:00,completeDate=2019-04-03T11:35:13.050+02:00,sequence=6669,goal=<null>]",
            "com.atlassian.greenhopper.service.sprint.Sprint@1e9bdaba[id=6555,rapidViewId=5369,state=CLOSED,name=Book Sprint 0,startDate=2019-01-03T16:08:00.338+01:00,endDate=2019-01-15T16:08:00.000+01:00,completeDate=2019-01-16T13:01:15.904+01:00,sequence=6555,goal=<null>]",
            "com.atlassian.greenhopper.service.sprint.Sprint@4928a21e[id=100500,rapidViewId=5967,state=ACTIVE,name=CS Sprint 0,startDate=2019-07-08T09:39:01.891+02:00,endDate=2019-07-19T09:39:00.000+02:00,completeDate=<null>,sequence=7789,goal=<null>]"
        ));

        JiraHelper helper = JiraHelper.getClient(jiraRestClientMock, false);
        SprintDto sprint = helper.getActiveSprint("TEST_ID");

        Assertions.assertThat(sprint.getId()).isEqualTo(100500);
        Assertions.assertThat(sprint.getRapidViewId()).isEqualTo(5967);
        Assertions.assertThat(sprint.getState()).isEqualTo("ACTIVE");
        Assertions.assertThat(sprint.getName()).isEqualTo("CS Sprint 0");
        Assertions.assertThat(sprint.getStartDateString()).isEqualTo("2019-07-08T09:39:01.891+02:00");
        Assertions.assertThat(sprint.getStartDate()).hasYear(2019);
        Assertions.assertThat(sprint.getStartDate()).hasMonth(7);
        Assertions.assertThat(sprint.getStartDate()).hasDayOfMonth(8);
        Assertions.assertThat(sprint.getStartDate()).hasHourOfDay(9);
        Assertions.assertThat(sprint.getEndDateString()).isEqualTo("2019-07-19T09:39:00.000+02:00");
        Assertions.assertThat(sprint.getEndDate()).hasYear(2019);
        Assertions.assertThat(sprint.getEndDate()).hasMonth(7);
        Assertions.assertThat(sprint.getEndDate()).hasDayOfMonth(19);
        Assertions.assertThat(sprint.getEndDate()).hasHourOfDay(9);
        Assertions.assertThat(sprint.getCompleteDateString()).isEqualTo("<null>");
        Assertions.assertThat(sprint.getCompleteDate()).isNull();
        Assertions.assertThat(sprint.getSequence()).isEqualTo(7789);
        Assertions.assertThat(sprint.getGoal()).isEqualTo("<null>");

    }
}