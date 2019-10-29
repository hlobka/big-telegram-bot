package telegram.bot.metrics.jira;

import atlassian.jira.JiraHelper;
import atlassian.jira.JqlCriteria;
import atlassian.jira.subclient.VersionDto;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Version;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JiraAllPeriodMetricsCollectorTest {

    private JiraHelper jiraHelper = Mockito.mock(JiraHelper.class);
    private FavoriteJqlRules jiraConfig = Mockito.mock(FavoriteJqlRules.class);
    private JiraAllPeriodMetricsCollector testTarget;
    private int expectedOriginalEstimate = 112;
    private int maxPerProperty = 100;
    private float expectedPv = expectedOriginalEstimate;

    @BeforeMethod
    public void setUp() {

        Mockito.when(jiraConfig.getAllIssuesJql(Mockito.anyString())).thenReturn("any1");
        Mockito.when(jiraConfig.getAllEstimatedOrTrackedIssues(Mockito.anyString())).thenReturn("any2");
        Mockito.when(jiraConfig.getAllClosedAndEstimatedOrTrackedIssues(Mockito.anyString())).thenReturn("any3");
        Mockito.when(jiraConfig.getSprintAllIssuesJql(Mockito.anyString())).thenReturn("any4");
        Mockito.when(jiraConfig.getSprintClosedIssuesJql(Mockito.anyString())).thenReturn("any5");
        Mockito.when(jiraConfig.getSprintActiveIssuesJql(Mockito.anyString())).thenReturn("any6");
        Mockito.when(jiraConfig.getSprintOpenIssuesJql(Mockito.anyString())).thenReturn("any7");
        Mockito.when(jiraConfig.getSprintUnEstimatedIssuesJql(Mockito.anyString())).thenReturn("any8");
        Mockito.when(jiraConfig.getSprintUnTrackedIssuesJql(Mockito.anyString())).thenReturn("any9");
        Mockito.when(jiraConfig.getSprintClosedAndUnTrackedIssuesJql(Mockito.anyString())).thenReturn("any10");

        testTarget = new JiraAllPeriodMetricsCollector(jiraHelper, jiraConfig, "TEST");
        testTarget.setLimitedMaxPerQuery(maxPerProperty);
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(1, expectedOriginalEstimate)
        );
        String jql = jiraConfig.getAllEstimatedOrTrackedIssues("TEST");
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JiraAllPeriodMetricsCollector.JIRA_FIELDS).withMaxPerQuery(maxPerProperty);
        Mockito.when(jiraHelper.getIssues(jql, jqlCriteria)).thenReturn(mockedIssues);
        Project project = Mockito.mock(Project.class);
        Mockito.when(jiraHelper.getProject("TEST")).thenReturn(project);
        ArrayList<Version> versions = new ArrayList<>();
        versions.add(Mockito.mock(Version.class));
        versions.add(Mockito.mock(Version.class));
        versions.add(Mockito.mock(Version.class));
        versions.add(Mockito.mock(Version.class));
        versions.add(Mockito.mock(Version.class));

        Mockito.when(project.getVersions()).thenReturn(versions);
        Mockito.when(versions.get(0).getSelf()).thenReturn(URI.create("https://test-jira.com/0"));
        Mockito.when(versions.get(1).getSelf()).thenReturn(URI.create("https://test-jira.com/1"));
        Mockito.when(versions.get(2).getSelf()).thenReturn(URI.create("https://test-jira.com/2"));
        Mockito.when(versions.get(3).getSelf()).thenReturn(URI.create("https://test-jira.com/3"));
        Mockito.when(versions.get(4).getSelf()).thenReturn(URI.create("https://test-jira.com/4"));

        Mockito.when(jiraHelper.getVersion(versions.get(0).getSelf())).thenReturn(Mockito.mock(VersionDto.class));
        Mockito.when(jiraHelper.getVersion(versions.get(1).getSelf())).thenReturn(Mockito.mock(VersionDto.class));
        Mockito.when(jiraHelper.getVersion(versions.get(2).getSelf())).thenReturn(Mockito.mock(VersionDto.class));
        Mockito.when(jiraHelper.getVersion(versions.get(3).getSelf())).thenReturn(Mockito.mock(VersionDto.class));
        Mockito.when(jiraHelper.getVersion(versions.get(4).getSelf())).thenReturn(Mockito.mock(VersionDto.class));

        long weekInMillis = TimeUnit.DAYS.toMillis(7);
        long time = new Date().getTime();
        DateTime startTime = new DateTime(new Date(time - weekInMillis).getTime());
        DateTime endTime = new DateTime(new Date(time + weekInMillis).getTime());

        Mockito.when(jiraHelper.getVersion(versions.get(0).getSelf()).getStartDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(1).getSelf()).getStartDate()).thenReturn(startTime);
        Mockito.when(jiraHelper.getVersion(versions.get(2).getSelf()).getStartDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(3).getSelf()).getStartDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(4).getSelf()).getStartDate()).thenReturn(null);

        Mockito.when(jiraHelper.getVersion(versions.get(0).getSelf()).getReleaseDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(1).getSelf()).getReleaseDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(2).getSelf()).getReleaseDate()).thenReturn(endTime);
        Mockito.when(jiraHelper.getVersion(versions.get(3).getSelf()).getReleaseDate()).thenReturn(null);
        Mockito.when(jiraHelper.getVersion(versions.get(4).getSelf()).getReleaseDate()).thenReturn(null);
    }

    @Test
    public void testGetProgressFactor() {
        Float progressFactor = testTarget.getProgressFactor();

        Assertions.assertThat(progressFactor)
            .as("progressFactor")
            .isBetween(0.49f, 0.51f);
    }

    @Test
    public void testProjectTotalHours() {

        Long totalTime = testTarget.getProjectTotalHours(TimeUnit.MINUTES);

        Assertions.assertThat(totalTime).isEqualTo(expectedOriginalEstimate);
    }

    @Test
    public void testGetOriginalEstimateMinutes() {
        List<Issue> mockedIssues = Arrays.asList(
            Mockito.mock(Issue.class),
            Mockito.mock(Issue.class),
            Mockito.mock(Issue.class)
        );
        TimeTracking mockedTimeTracking = Mockito.mock(TimeTracking.class);
        Mockito.when(mockedTimeTracking.getOriginalEstimateMinutes()).thenReturn(30, 20, 50);
        Mockito.when(mockedIssues.get(0).getTimeTracking()).thenReturn(mockedTimeTracking);
        Mockito.when(mockedIssues.get(1).getTimeTracking()).thenReturn(mockedTimeTracking);
        Mockito.when(mockedIssues.get(2).getTimeTracking()).thenReturn(mockedTimeTracking);

        Long totalTime = testTarget.getIssuesOriginalTotalMinutes(mockedIssues);

        Assertions.assertThat(totalTime).isEqualTo(100);
    }

    @Test
    public void testGetPlannedValue() {
        Double pv = testTarget.collect(TimeUnit.MINUTES).getPlannedValue();

        Assertions.assertThat(pv)
            .isBetween(expectedPv * .5 - 0.01d, expectedPv * .5 + 0.01d);

    }

    @Test
    public void testEarnedValue() {
        mockEarnedValueResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        long earnedValue = testTarget.collect(TimeUnit.MINUTES).getEarnedValue();

        Assertions.assertThat(earnedValue)
            .isEqualTo(6);

    }

    @Test
    public void testGetActualCost() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        long actualCost = testTarget.collect(TimeUnit.MINUTES).getActualCost();

        Assertions.assertThat(actualCost)
            .isEqualTo(6);
    }

    @Test
    public void testGetScheduleVariance() {
        double scheduleVariance = testTarget.collect(TimeUnit.MINUTES).getScheduleVariance();

        Assertions.assertThat(scheduleVariance)
            .isEqualTo(-50);
    }

    @Test
    public void testGetSchedulePerformanceIndex() {
        double schedulePerformanceIndex = testTarget.collect(TimeUnit.MINUTES).getSchedulePerformanceIndex();

        Assertions.assertThat(schedulePerformanceIndex)
            .isEqualTo(0.10714285714285714);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostAndEarnedValue() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.collect(TimeUnit.MINUTES).getCostVariance();

        Assertions.assertThat(costVariance)
            .isEqualTo(0);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostBiggestThanEarnedValue() {
        mockActualCostResult("TEST", new int[] {2, 2, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {2, 2, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.collect(TimeUnit.MINUTES).getCostVariance();

        Assertions.assertThat(costVariance)
            .isLessThan(0);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostLowerThanEarnedValue() {
        mockActualCostResult("TEST", new int[] {1, 1, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {1, 1, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.collect(TimeUnit.MINUTES).getCostVariance();

        Assertions.assertThat(costVariance)
            .isGreaterThan(0);
    }

    @Test
    public void testGetCostPerformanceIndex() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        double costPerformanceIndex = testTarget.collect(TimeUnit.MINUTES).getCostPerformanceIndex();

        Assertions.assertThat(costPerformanceIndex)
            .isEqualTo(1);
    }

    @Test
    public void testGetBudgetAtCompletion() {
        double budgetAtCompletion = testTarget.collect(TimeUnit.MINUTES).getBudgetAtCompletion();

        Assertions.assertThat(budgetAtCompletion)
            .isEqualTo(112.0);
    }

    @Test
    public void testGetEstimateAtCompletion() {
        double estimateAtCompletion = testTarget.collect(TimeUnit.MINUTES).getEstimateAtCompletion();

        Assertions.assertThat(estimateAtCompletion)
            .isEqualTo(18.666666666666668);
    }

    @Test
    public void testGetEstimateToComplete() {
        double estimateAtComplete = testTarget.collect(TimeUnit.MINUTES).getEstimateToComplete();

        Assertions.assertThat(estimateAtComplete)
            .isEqualTo(17.666666666666668);
    }

    @Test
    public void testGetVarianceAtCompletion() {
        double varianceAtCompletion = testTarget.collect(TimeUnit.MINUTES).getVarianceAtCompletion();

        Assertions.assertThat(varianceAtCompletion)
            .isEqualTo(93.33333333333333);
    }


    private void mockEarnedValueResult(String projectId, int[] timeSpentMinutes, int[] originalEstimateMinutes) {
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[0], originalEstimateMinutes[0]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[1], originalEstimateMinutes[1]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[2], originalEstimateMinutes[2])
        );
        String jql = jiraConfig.getAllClosedAndEstimatedOrTrackedIssues(projectId);
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JiraAllPeriodMetricsCollector.JIRA_FIELDS).withMaxPerQuery(maxPerProperty);
        Mockito.when(jiraHelper.getIssues(jql, jqlCriteria)).thenReturn(mockedIssues);
    }

    private void mockActualCostResult(String projectId, int[] timeSpentMinutes, int[] originalEstimateMinutes) {
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[0], originalEstimateMinutes[0]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[1], originalEstimateMinutes[1]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[2], originalEstimateMinutes[2])
        );
        String jql = jiraConfig.getAllEstimatedOrTrackedIssues(projectId);
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JiraAllPeriodMetricsCollector.JIRA_FIELDS).withMaxPerQuery(maxPerProperty);
        Mockito.when(jiraHelper.getIssues(jql, jqlCriteria)).thenReturn(mockedIssues);
    }

    private Issue getMockedIssueWithMockedTimeTracking(int timeSpentMinutes, int originalEstimateMinutes) {
        return getMockedIssueWithMockedTimeTracking(Mockito.mock(Issue.class), timeSpentMinutes, originalEstimateMinutes);
    }

    private Issue getMockedIssueWithMockedTimeTracking(Issue issue, int timeSpentMinutes, int originalEstimateMinutes) {
        TimeTracking mockedTimeTracking = Mockito.mock(TimeTracking.class);
        Mockito.when(mockedTimeTracking.getOriginalEstimateMinutes()).thenReturn(originalEstimateMinutes);
        Mockito.when(mockedTimeTracking.getTimeSpentMinutes()).thenReturn(timeSpentMinutes);
        Mockito.when(issue.getTimeTracking()).thenReturn(mockedTimeTracking);
        return issue;
    }
}