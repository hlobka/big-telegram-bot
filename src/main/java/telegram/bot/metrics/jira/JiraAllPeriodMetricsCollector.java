package telegram.bot.metrics.jira;

import atlassian.jira.JiraHelper;
import atlassian.jira.JqlCriteria;
import atlassian.jira.subclient.VersionDto;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import lombok.Data;
import org.joda.time.DateTime;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JiraAllPeriodMetricsCollector implements JiraMetricsCollector {

    static final Set<String> JIRA_FIELDS = new HashSet<>(Arrays.asList("timetracking", "summary", "issuetype", "created", "updated", "project", "status"));;

    private final JiraHelper jiraHelper;
    private final FavoriteJqlRules jiraConfig;
    private final String projectKey;
    private Map<String, List<Issue>> issuesCache;
    private final int maxPerQuery;
    private int limitedMaxPerQuery;

    public JiraAllPeriodMetricsCollector(JiraHelper jiraHelper, FavoriteJqlRules jiraConfig, String projectKey) {
        this(jiraHelper, jiraConfig, projectKey, 1000);
    }

    public JiraAllPeriodMetricsCollector(JiraHelper jiraHelper, FavoriteJqlRules jiraConfig, String projectKey, int maxPerQuery) {
        this.jiraHelper = jiraHelper;
        this.jiraConfig = jiraConfig;
        this.projectKey = projectKey;
        this.maxPerQuery = maxPerQuery;
        this.limitedMaxPerQuery = 100;
        this.issuesCache = new HashMap<>();
    }

    public void setLimitedMaxPerQuery(int limitedMaxPerQuery) {
        this.limitedMaxPerQuery = limitedMaxPerQuery;
    }

    Long getProjectTotalHours(TimeUnit timeUnit) {
        String jql = jiraConfig.getAllEstimatedOrTrackedIssues(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JIRA_FIELDS).withMaxPerQuery(maxPerQuery);
        List<Issue> issues = getIssues(jql, jqlCriteria);
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
        String jql = jiraConfig.getAllClosedAndEstimatedOrTrackedIssues(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JIRA_FIELDS).withMaxPerQuery(maxPerQuery);
        List<Issue> issues = getIssues(jql, jqlCriteria);
        return getIssuesOriginalTotalTimeIn(issues, timeUnit);
    }

    private long getActualCost(TimeUnit timeUnit) {
        String jql = jiraConfig.getAllEstimatedOrTrackedIssues(projectKey);
        JqlCriteria jqlCriteria = new JqlCriteria().withFields(JIRA_FIELDS).withMaxPerQuery(maxPerQuery);
        List<Issue> issues = getIssues(jql, jqlCriteria);
        return getIssuesSpentTotalTimeIn(issues, timeUnit);
    }

    private List<Issue> getIssues(String jql, JqlCriteria jqlCriteria) {
        if (issuesCache.containsKey(jql)) {
            return issuesCache.get(jql);
        }
        List<Issue> result = new ArrayList<>();
        Integer maxPerQuery = jqlCriteria.getMaxPerQuery();
        int startIndex = 0;
        if (maxPerQuery > limitedMaxPerQuery && jqlCriteria.getStartIndex() == 0) {
            while (startIndex < maxPerQuery) {
                jqlCriteria.setStartIndex(startIndex);
                jqlCriteria.setMaxPerQuery(limitedMaxPerQuery);
                List<Issue> issues = jiraHelper.getIssues(jql, jqlCriteria);
                if (issues.isEmpty()) {
                    break;
                }
                result.addAll(issues);
                startIndex += limitedMaxPerQuery;
            }
        } else {
            result.addAll(jiraHelper.getIssues(jql, jqlCriteria));
        }
        issuesCache.put(jql, result);
        return result;
    }

    Float getProgressFactor() {
        ProjectTimePeriod projectTimePeriod = getProjectTimePeriod(projectKey);
        return getProgressFactor(projectTimePeriod);
    }

    private Float getProgressFactor(ProjectTimePeriod projectTimePeriod) {
        long projectDuration = getProjectDuration(projectTimePeriod);
        long actualTime = new Date().getTime();
        long sprintProgress = actualTime - projectTimePeriod.getStartDate().getTime();
        return (float) sprintProgress / projectDuration;
    }

    private long getProjectDuration(ProjectTimePeriod projectTimePeriod) {
        return projectTimePeriod.getEndDate().getTime() - projectTimePeriod.getStartDate().getTime();
    }

    private ProjectTimePeriod getProjectTimePeriod(String projectKey) {
        Project project = jiraHelper.getProject(projectKey);
        List<Version> versions = (List<Version>) project.getVersions();
        if (versions == null || versions.isEmpty()) {
            throw new RuntimeException("No available versions for: " + projectKey);
        }
        Date startDate = new Date(Long.MAX_VALUE);
        Date endDate = new Date(0);
        for (Version version : versions) {
            VersionDto versionDto = jiraHelper.getVersion(version.getSelf());
            DateTime startDateTime = versionDto.getStartDate();
            if (startDateTime != null && startDate.getTime() > startDateTime.getMillis()) {
                startDate = new Date(startDateTime.getMillis());
            }
            DateTime releaseDate = versionDto.getReleaseDate();
            if (releaseDate != null && endDate.getTime() < releaseDate.getMillis()) {
                endDate = new Date(releaseDate.getMillis());
            }
        }
        if (startDate.equals(new Date(Long.MAX_VALUE))) {
            throw new RuntimeException("No available versions with startDate for: " + projectKey);
        }
        if (endDate.equals(new Date(0))) {
            throw new RuntimeException("No available versions with endDate for: " + projectKey);
        }
        return new ProjectTimePeriod(projectKey, startDate, endDate);
    }

    private double getBudgetAtCompletion(TimeUnit timeUnit) {
        return getProjectTotalHours(timeUnit);
    }

    @Override
    public JiraMetricsProvider collect(TimeUnit timeUnit, ProgressListener progressListener) {
        issuesCache = new HashMap<>();
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
        return collect(timeUnit, (currentStep, maxSteps) -> {
        });
    }

    @Data
    private static class ProjectTimePeriod {
        private final String projectKey;
        private final Date startDate;
        private final Date endDate;
    }
}
