package telegram.bot.metrics.jira;

import atlassian.jira.JiraHelper;
import atlassian.jira.JqlCriteria;
import atlassian.jira.SprintDto;
import com.atlassian.jira.rest.client.api.domain.Issue;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class JiraSprintMetricsCollector implements JiraMetricsCollector {

    private final JiraHelper jiraHelper;
    private final FavoriteJqlRules jiraConfig;
    private final String projectKey;

    public JiraSprintMetricsCollector(JiraHelper jiraHelper, FavoriteJqlRules jiraConfig, String projectKey) {
        this.jiraHelper = jiraHelper;
        this.jiraConfig = jiraConfig;
        this.projectKey = projectKey;
    }

    Long getActiveSprintTotalHours(TimeUnit timeUnit) {
        String jql = jiraConfig.getSprintAllIssuesJql(projectKey);
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
        String jql = jiraConfig.getSprintClosedIssuesJql(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
        return getIssuesOriginalTotalTimeIn(issues, timeUnit);
    }

    private long getActualCost(TimeUnit timeUnit) {
        String jql = jiraConfig.getSprintAllIssuesJql(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFillInformation(true);
        List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
        return getIssuesSpentTotalTimeIn(issues, timeUnit);
    }

    Float getProgressFactor() {
        SprintDto activeSprint = jiraHelper.getActiveSprint(projectKey);
        return getProgressFactor(activeSprint);
    }

    private Float getProgressFactor(SprintDto sprint) {
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

    @Override
    public JiraMetricsProvider collect(TimeUnit timeUnit, ProgressListener progressListener) {
        progressListener.update(0, 4);
        double budgetAtCompletion = getBudgetAtCompletion(timeUnit);
        progressListener.update(1, 4);
        Float progressFactor = getProgressFactor();
        progressListener.update(2, 4);
        long earnedValue = getEarnedValue(timeUnit);
        progressListener.update(3, 4);
        long actualCost = getActualCost(timeUnit);
        progressListener.update(4, 4);
        return new JiraMetricsProvider(
            timeUnit,
            budgetAtCompletion,
            progressFactor,
            earnedValue,
            actualCost
        );
    }

    @Override
    public JiraMetricsProvider collect(TimeUnit timeUnit) {
        return collect(timeUnit, (currentStep, maxSteps) -> {});
    }
}
