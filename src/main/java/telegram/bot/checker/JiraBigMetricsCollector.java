package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import atlassian.jira.SprintDto;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class JiraBigMetricsCollector {

    private final JiraHelper jiraHelper;

    public JiraBigMetricsCollector(JiraHelper jiraHelper) {
        this.jiraHelper = jiraHelper;
    }

    public Long getActiveSprintTotalHours(String projectKey) {
        List<Issue> issues = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectKey));
        return getIssuesOriginalTotalHours(issues);
    }

    public long getIssuesSpentTotalHours(List<Issue> issues) {
        return issues
            .stream()
            .filter(issue -> issue.getTimeTracking() != null)
            .map(issue -> issue.getTimeTracking().getTimeSpentMinutes())
            .filter(Objects::nonNull)
            .mapToLong(Integer::longValue)
            .sum();

    }

    public long getIssuesOriginalTotalHours(List<Issue> issues) {
        return issues
            .stream()
            .filter(issue -> issue.getTimeTracking() != null)
            .map(issue -> issue.getTimeTracking().getOriginalEstimateMinutes())
            .filter(Objects::nonNull)
            .mapToLong(Integer::longValue)
            .sum();
    }

    public long getEarnedValue(String projectKey) {
        List<Issue> issues = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintClosedIssuesJql(projectKey));
        return getIssuesOriginalTotalHours(issues);
    }

    public long getActualCost(String projectKey) {
        List<Issue> issues = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectKey));
        return getIssuesSpentTotalHours(issues);
    }

    public Double getPlannedValue(String projectKey) {
        Long th = getActiveSprintTotalHours(projectKey);
        Float sprintProgressFactor = getSprintProgressFactor(projectKey);

        return (double) (th * sprintProgressFactor);
    }

    public Float getSprintProgressFactor(String projectKey) {
        SprintDto activeSprint = jiraHelper.getActiveSprint(projectKey);
        return getSprintProgressFactor(activeSprint);
    }

    public Float getSprintProgressFactor(SprintDto sprint) {
        long sprintDuration = getSprintDuration(sprint);
        long actualTime = new Date().getTime();
        long sprintProgress = actualTime - sprint.getStartDate().getTime();
        return (float) sprintProgress / sprintDuration;
    }

    public long getSprintDuration(String projectKey) {
        SprintDto activeSprint = jiraHelper.getActiveSprint(projectKey);
        return getSprintDuration(activeSprint);
    }

    public long getSprintDuration(SprintDto sprint) {
        Date startDate = sprint.getStartDate();
        Date endDate = sprint.getEndDate();
        long sprintDuration = endDate.getTime() - startDate.getTime();
        return sprintDuration;
    }

    public double getScheduleVariance(String projectKey) {
        return getEarnedValue(projectKey) - getPlannedValue(projectKey);
    }

    public double getSchedulePerformanceIndex(String projectKey) {
        return getEarnedValue(projectKey) / getPlannedValue(projectKey);
    }

    public double getCostVariance(String projectKey) {
        return getEarnedValue(projectKey) - getActualCost(projectKey);
    }

    public double getCostPerformanceIndex(String projectKey) {
        return getEarnedValue(projectKey) / getActualCost(projectKey);
    }

    public double getBudgetAtCompletion(String projectKey) {
        return getActiveSprintTotalHours(projectKey);
    }

    public double getEstimateAtCompletion(String projectKey) {
        return getBudgetAtCompletion(projectKey) / getCostPerformanceIndex(projectKey);
    }

    public double getEstimateAtComplete(String projectKey) {
        return getEstimateAtCompletion(projectKey) - getActualCost(projectKey);
    }

    public double getVarianceAtCompletion(String projectKey) {
        return getBudgetAtCompletion(projectKey) - getEstimateAtCompletion(projectKey);
    }
}
