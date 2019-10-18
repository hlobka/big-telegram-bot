package telegram.bot.metrics.jira;

import atlassian.jira.JiraHelper;
import atlassian.jira.JqlCriteria;
import atlassian.jira.SprintDto;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JiraSprintMetricsCollectorTest {

    private JiraHelper jiraHelper = Mockito.mock(JiraHelper.class);
    private FavoriteJqlRules jiraConfig = Mockito.mock(FavoriteJqlRules.class);
    private JiraSprintMetricsCollector testTarget;
    private int expectedOriginalEstimate = 112;
    private float expectedPv = expectedOriginalEstimate;

    @BeforeMethod
    public void setUp() {

        Mockito.when(jiraConfig.getAllIssuesJql(Mockito.anyString())).thenReturn("any1");
        Mockito.when(jiraConfig.getSprintAllIssuesJql(Mockito.anyString())).thenReturn("any2");
        Mockito.when(jiraConfig.getSprintClosedIssuesJql(Mockito.anyString())).thenReturn("any3");
        Mockito.when(jiraConfig.getSprintActiveIssuesJql(Mockito.anyString())).thenReturn("any4");
        Mockito.when(jiraConfig.getSprintOpenIssuesJql(Mockito.anyString())).thenReturn("any5");
        Mockito.when(jiraConfig.getSprintUnEstimatedIssuesJql(Mockito.anyString())).thenReturn("any6");
        Mockito.when(jiraConfig.getSprintUnTrackedIssuesJql(Mockito.anyString())).thenReturn("any7");
        Mockito.when(jiraConfig.getSprintClosedAndUnTrackedIssuesJql(Mockito.anyString())).thenReturn("any8");

        testTarget = new JiraSprintMetricsCollector(jiraHelper, jiraConfig, "TEST");
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(1, expectedOriginalEstimate)
        );
        String jql = jiraConfig.getSprintAllIssuesJql("TEST");
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        Mockito.when(jiraHelper.getIssues(jql, jqlCriteria)).thenReturn(mockedIssues);
        SprintDto sprint = Mockito.mock(SprintDto.class);
        Mockito.when(jiraHelper.getActiveSprint("TEST")).thenReturn(sprint);
        long weekInMillis = TimeUnit.DAYS.toMillis(7);
        long time = new Date().getTime();
        Mockito.when(sprint.getStartDate()).thenReturn(new Date(time - weekInMillis));
        Mockito.when(sprint.getEndDate()).thenReturn(new Date(time + weekInMillis));
    }

    @Test
    public void testGetSprintProgressFactor() {
        Float sprintProgressFactor = testTarget.getProgressFactor();

        Assertions.assertThat(sprintProgressFactor)
            .as("sprintProgressFactor")
            .isBetween(0.49f, 0.51f);
    }

    @Test
    public void testGetActiveSprintTotalHours() {

        Long totalTime = testTarget.getActiveSprintTotalHours(TimeUnit.MINUTES);

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
        String jql = jiraConfig.getSprintClosedIssuesJql(projectId);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        Mockito.when(jiraHelper.getIssues(jql, jqlCriteria)).thenReturn(mockedIssues);
    }

    private void mockActualCostResult(String projectId, int[] timeSpentMinutes, int[] originalEstimateMinutes) {
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[0], originalEstimateMinutes[0]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[1], originalEstimateMinutes[1]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[2], originalEstimateMinutes[2])
        );
        String jql = jiraConfig.getSprintAllIssuesJql(projectId);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
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