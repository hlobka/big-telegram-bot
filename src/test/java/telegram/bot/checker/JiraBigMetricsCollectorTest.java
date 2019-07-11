package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import atlassian.jira.SprintDto;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JiraBigMetricsCollectorTest {

    private JiraHelper jiraHelper = Mockito.mock(JiraHelper.class);
    private JiraBigMetricsCollector testTarget;
    private int expectedOriginalEstimate = 112;
    private float expectedPv = expectedOriginalEstimate;

    @BeforeMethod
    public void setUp() {
        testTarget = new JiraBigMetricsCollector(jiraHelper);
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(1, expectedOriginalEstimate)
        );
        Mockito.when(jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintAllIssuesJql("TEST"))).thenReturn(mockedIssues);
        SprintDto sprint = Mockito.mock(SprintDto.class);
        Mockito.when(jiraHelper.getActiveSprint("TEST")).thenReturn(sprint);
        long weekInMillis = TimeUnit.DAYS.toMillis(7);
        long time = new Date().getTime();
        Mockito.when(sprint.getStartDate()).thenReturn(new Date(time - weekInMillis));
        Mockito.when(sprint.getEndDate()).thenReturn(new Date(time + weekInMillis));
    }

    @Test
    public void testGetSprintProgressFactor() {
        Float sprintProgressFactor = testTarget.getSprintProgressFactor("TEST");

        Assertions.assertThat(sprintProgressFactor)
            .as("sprintProgressFactor")
            .isBetween(0.49f, 0.51f);
    }

    @Test
    public void testGetActiveSprintTotalHours() {

        Long totalTime = testTarget.getActiveSprintTotalHours("TEST");

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

        Long totalTime = testTarget.getIssuesOriginalTotalHours(mockedIssues);

        Assertions.assertThat(totalTime).isEqualTo(100);
    }

    @Test
    public void testGetPlannedValue() {
        Double pv = testTarget.getPlannedValue("TEST");

        Assertions.assertThat(pv)
            .isBetween(expectedPv * .5 - 0.01d, expectedPv * .5 + 0.01d);

    }

    @Test
    public void testEarnedValue() {
        mockEarnedValueResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        long earnedValue = testTarget.getEarnedValue("TEST");

        Assertions.assertThat(earnedValue)
            .isEqualTo(6);

    }

    @Test
    public void testGetActualCost() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        long actualCost = testTarget.getActualCost("TEST");

        Assertions.assertThat(actualCost)
            .isEqualTo(6);
    }

    @Test
    public void testGetScheduleVariance() {
        double scheduleVariance = testTarget.getScheduleVariance("TEST");

        Assertions.assertThat(scheduleVariance)
            .isEqualTo(-50);
    }

    @Test
    public void testGetSchedulePerformanceIndex() {
        double schedulePerformanceIndex = testTarget.getSchedulePerformanceIndex("TEST");

        Assertions.assertThat(schedulePerformanceIndex)
            .isEqualTo(0.10714285714285714);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostAndEarnedValue() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.getCostVariance("TEST");

        Assertions.assertThat(costVariance)
            .isEqualTo(0);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostBiggestThanEarnedValue() {
        mockActualCostResult("TEST", new int[] {2, 2, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {2, 2, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.getCostVariance("TEST");

        Assertions.assertThat(costVariance)
            .isLessThan(0);
    }

    @Test
    public void testGetCostVarianceWhenEqualsActualCostLowerThanEarnedValue() {
        mockActualCostResult("TEST", new int[] {1, 1, 3}, new int[] {1, 2, 3});
        mockEarnedValueResult("TEST", new int[] {1, 1, 3}, new int[] {1, 2, 3});

        double costVariance = testTarget.getCostVariance("TEST");

        Assertions.assertThat(costVariance)
            .isGreaterThan(0);
    }

    @Test
    public void testGetCostPerformanceIndex() {
        mockActualCostResult("TEST", new int[] {1, 2, 3}, new int[] {1, 2, 3});

        double costPerformanceIndex = testTarget.getCostPerformanceIndex("TEST");

        Assertions.assertThat(costPerformanceIndex)
            .isEqualTo(1);
    }

    @Test
    public void testGetBudgetAtCompletion() {
        double budgetAtCompletion = testTarget.getBudgetAtCompletion("TEST");

        Assertions.assertThat(budgetAtCompletion)
            .isEqualTo(112.0);
    }

    @Test
    public void testGetEstimateAtCompletion() {
        double estimateAtCompletion = testTarget.getEstimateAtCompletion("TEST");

        Assertions.assertThat(estimateAtCompletion)
            .isEqualTo(18.666666666666668);
    }

    @Test
    public void testGetEstimateToComplete() {
        double estimateAtComplete = testTarget.getEstimateAtComplete("TEST");

        Assertions.assertThat(estimateAtComplete)
            .isEqualTo(17.666666666666668);
    }

    @Test
    public void testGetVarianceAtCompletion() {
        double varianceAtCompletion = testTarget.getVarianceAtCompletion("TEST");

        Assertions.assertThat(varianceAtCompletion)
            .isEqualTo(93.33333333333333);
    }


    private void mockEarnedValueResult(String projectId, int[] timeSpentMinutes, int[] originalEstimateMinutes) {
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[0], originalEstimateMinutes[0]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[1], originalEstimateMinutes[1]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[2], originalEstimateMinutes[2])
        );
        Mockito.when(jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintClosedIssuesJql(projectId))).thenReturn(mockedIssues);
    }

    private void mockActualCostResult(String projectId, int[] timeSpentMinutes, int[] originalEstimateMinutes) {
        List<Issue> mockedIssues = Arrays.asList(
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[0], originalEstimateMinutes[0]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[1], originalEstimateMinutes[1]),
            getMockedIssueWithMockedTimeTracking(timeSpentMinutes[2], originalEstimateMinutes[2])
        );
        Mockito.when(jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectId))).thenReturn(mockedIssues);
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