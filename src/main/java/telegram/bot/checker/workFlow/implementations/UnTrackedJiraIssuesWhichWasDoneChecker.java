package telegram.bot.checker.workFlow.implementations;

import atlassian.jira.FavoriteJqlScriptHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import telegram.bot.checker.JiraCheckerHelper;
import telegram.bot.checker.workFlow.ChatChecker;
import telegram.bot.checker.workFlow.implementations.services.JiraHelperServiceProvider;
import telegram.bot.data.chat.ChatData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static helper.logger.ConsoleLogger.logFor;

public class UnTrackedJiraIssuesWhichWasDoneChecker implements ChatChecker {

    private final JiraHelperServiceProvider jiraHelperServiceProvider;

    public UnTrackedJiraIssuesWhichWasDoneChecker(JiraHelperServiceProvider jiraHelperServiceProvider) {
        this.jiraHelperServiceProvider = jiraHelperServiceProvider;
    }

    @Override
    public Boolean isAccessibleToCheck(ChatData chatData) {
        return !chatData.getJiraProjectKeyIds().isEmpty() && chatData.getIsEstimationRequired();
    }

    @Override
    public List<String> check(ChatData chatData) {
        logFor(this, "check:start");
        List<String> result = new ArrayList<>();
        jiraHelperServiceProvider.provide(jiraHelper -> {
            for (String jiraProjectKeyId : chatData.getJiraProjectKeyIds()) {
                String jql = FavoriteJqlScriptHelper.getSprintClosedAndUnTrackedIssuesJql(jiraProjectKeyId) + " AND status != Rejected";
                List<Issue> issues = jiraHelper.getIssues(jql, true);
                issues = issues.stream().filter(this::isJiraNotTimeTracked).collect(Collectors.toList());
                result.addAll(getUnTrackedMessages(issues));
                if (issues.size() > 0) {
                    result.add(getExpectedLostTimeMessage(issues));
                }
            }
        });
        logFor(this, "check:end");
        return result;
    }

    public String getExpectedLostTimeMessage(List<Issue> issues) {
        String result = "";
        Integer lostTimeInMinutes = 0;
        for (Issue issue : issues) {
            if (issue.getTimeTracking() != null && issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
                lostTimeInMinutes += issue.getTimeTracking().getOriginalEstimateMinutes();
            }
            Boolean jiraTimeTracked = isJiraTimeTracked(issue);
            if (!jiraTimeTracked) {

            }
        }
        result = "🔥🔥🔥🔥🔥🔥\n Общее время которое могло быть затреканно: ** \n" +
            getMinutesAsStringTime(lostTimeInMinutes);

        return result;
    }

    private String getMinutesAsStringTime(Integer lostTimeInMinutes) {
        long days = TimeUnit.MINUTES.toDays(lostTimeInMinutes);
        long hours = TimeUnit.MINUTES.toHours(lostTimeInMinutes - TimeUnit.DAYS.toMinutes(days));
        long minutes = TimeUnit.MINUTES.toHours(lostTimeInMinutes - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours));
        return String.format("%dd %dh %dm", days, hours, minutes);
    }

    public List<String> getUnTrackedMessages(List<Issue> issues) {
        List<String> result = new ArrayList<>();
        for (Issue issue : issues) {
            Boolean jiraTimeTracked = isJiraTimeTracked(issue);
            if (!jiraTimeTracked) {
                String message =
                    "🔥🔥🔥\n Данная задача закрыта без лога времени: ** " +
                        JiraCheckerHelper.getIssueDescription(issue);
                result.add(
                    message
                );
            }
        }
        return result;
    }

    private Boolean isJiraNotTimeTracked(Issue issue) {
        return !isJiraTimeTracked(issue);
    }

    private Boolean isJiraTimeTracked(Issue issue) {
        boolean result = false;
        TimeTracking timeTracking = issue.getTimeTracking();
        if (timeTracking != null) {
            Integer timeSpentMinutes = timeTracking.getTimeSpentMinutes();
            if (timeSpentMinutes != null && timeSpentMinutes > 0) {
                for (Worklog worklog : issue.getWorklogs()) {
                    if (!"code review".equalsIgnoreCase(worklog.getComment())) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }
}