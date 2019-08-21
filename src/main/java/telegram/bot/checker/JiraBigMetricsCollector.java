package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import atlassian.jira.JqlCriteria;
import atlassian.jira.SprintDto;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class JiraBigMetricsCollector {

    private final JiraHelper jiraHelper;
    private final String projectKey;

    public JiraBigMetricsCollector(JiraHelper jiraHelper, String projectKey) {
        this.jiraHelper = jiraHelper;
        this.projectKey = projectKey;
    }

    Long getActiveSprintTotalHours(TimeUnit timeUnit) {
        String jql = FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
        return getIssuesOriginalTotalTimeIn(issues, timeUnit);
    }

    private long getIssuesSpentTotalTimeIn(List<Issue> issues, TimeUnit timeUnit) {
        long minutes = getIssuesSpentTotalMinutes(issues);
        return timeUnit.convert(minutes, TimeUnit.MINUTES);
    }

    private long getIssuesSpentTotalMinutes(List<Issue> issues) {
        return issues
            .stream()
            .filter(issue -> issue.getTimeTracking() != null)
            .map(issue -> issue.getTimeTracking().getTimeSpentMinutes())
            .filter(Objects::nonNull)
            .mapToLong(Integer::longValue)
            .sum();
    }

    private long getIssuesOriginalTotalTimeIn(List<Issue> issues, TimeUnit timeUnit) {
        long minutes = getIssuesOriginalTotalMinutes(issues);
        return timeUnit.convert(minutes, TimeUnit.MINUTES);
    }

    long getIssuesOriginalTotalMinutes(List<Issue> issues) {
        return issues
            .stream()
            .filter(issue -> issue.getTimeTracking() != null)
            .map(issue -> issue.getTimeTracking().getOriginalEstimateMinutes())
            .filter(Objects::nonNull)
            .mapToLong(Integer::longValue)
            .sum();
    }

    private long getEarnedValue(TimeUnit timeUnit) {
        String jql = FavoriteJqlScriptHelper.getSprintClosedIssuesJql(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
        return getIssuesOriginalTotalTimeIn(issues, timeUnit);
    }

    private long getActualCost(TimeUnit timeUnit) {
        String jql = FavoriteJqlScriptHelper.getSprintAllIssuesJql(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
        return getIssuesSpentTotalTimeIn(issues, timeUnit);
    }

    Float getSprintProgressFactor() {
        SprintDto activeSprint = jiraHelper.getActiveSprint(projectKey);
        return getSprintProgressFactor(activeSprint);
    }

    private Float getSprintProgressFactor(SprintDto sprint) {
        long sprintDuration = getSprintDuration(sprint);
        long actualTime = new Date().getTime();
        long sprintProgress = actualTime - sprint.getStartDate().getTime();
        return (float) sprintProgress / sprintDuration;
    }

    private long getSprintDuration(SprintDto sprint) {
        Date startDate = sprint.getStartDate();
        Date endDate = sprint.getEndDate();
        return endDate.getTime() - startDate.getTime();
    }

    private double getBudgetAtCompletion(TimeUnit timeUnit) {
        return getActiveSprintTotalHours(timeUnit);
    }

    public JiraBigMetricsProvider collect(TimeUnit timeUnit) {
        return new JiraBigMetricsProvider(
            timeUnit,
            getBudgetAtCompletion(timeUnit),
            getSprintProgressFactor(),
            getEarnedValue(timeUnit),
            getActualCost(timeUnit)
        );
    }
}