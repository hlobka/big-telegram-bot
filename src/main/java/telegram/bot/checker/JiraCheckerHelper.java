package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import telegram.bot.data.Common;
import telegram.bot.data.jira.FavoriteJqlRules;
import telegram.bot.helper.BotHelper;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class JiraCheckerHelper {
    private final JiraHelper jiraHelper;

    public JiraCheckerHelper(JiraHelper jiraHelper) {
        this.jiraHelper = jiraHelper;
    }

    public String getIssueDescription(String issueKey) {
        Issue issue = jiraHelper.getIssue(issueKey);
        return getIssueDescription(issue);
    }

    public static String getIssueDescription(Issue issue) {
        String reporter = getName(issue.getReporter());
        String assignee = getName(issue.getAssignee());
        assignee = getUserLoginWithTelegramLinkOnUser(assignee);
        String summary = issue.getSummary();
        Object priority = issue.getPriority() == null ? "Low" : issue.getPriority().getName();
        String linkedIssueKey = JiraCheckerHelper.getTelegramIssueLink(issue);
        return String.format("%n%n %s as: ``` %s ``` Created by: *%s*,%n Assignee on: %s with Priority: * %s *", linkedIssueKey, summary, reporter, assignee, priority);
    }

    public static String getUserLoginWithTelegramLinkOnUser(User user) {
        String assignee = getName(user);
        return getUserLoginWithTelegramLinkOnUser(assignee);
    }

    private static String getUserLoginWithTelegramLinkOnUser(String userLogin) {
        Integer telegramId = Common.USER_LOGIN_ON_TELEGRAM_ID_MAP.getOrDefault(userLogin, 0);
        for (Map.Entry<com.pengrad.telegrambot.model.User, Boolean> entry : Common.ETS_HELPER.getUsers().entrySet()) {
            com.pengrad.telegrambot.model.User telegramUser = entry.getKey();
            if (telegramUser.id().equals(telegramId)) {
                return BotHelper.getLinkOnUser(telegramUser, userLogin, ParseMode.Markdown);
            }
        }
        return "*" + userLogin + "*";
    }

    private static String getName(User user) {
        return user == null ? "RIP" : user.getName();
    }

    public String getActiveSprintUnEstimatedIssuesMessage(String projectKey, FavoriteJqlRules jiraConfig) {
        List<Issue> issues = getActiveSprintUnEstimatedIssues(projectKey, jiraConfig);
        StringBuilder result = new StringBuilder();
        if (!issues.isEmpty()) {
            result.append("🔥🔥🔥\n");
            result.append("Данные задачи нуждаются в дополнительной эстимации:");
        }
        for (Issue issue : issues) {
            result.append(getIssueDescription(issue));
        }
        return result.toString();
    }

    public List<Issue> getActiveSprintUnEstimatedIssues(String projectKey, FavoriteJqlRules jiraConfig) {
        String jql = jiraConfig.getSprintUnEstimatedIssuesJql(projectKey);
        return jiraHelper.getIssues(jql);
    }

    public static String getTelegramIssueLink(Issue issue) {
        return getTelegramIssueLink(issue.getSelf().getScheme()+"://"+issue.getSelf().getHost(), issue.getKey());
    }

    public static String getTelegramIssueLink(String jiraUrl, String issueKey) {
        return BotHelper.getLink(String.format("%1$s/browse/%2$s", jiraUrl, issueKey), issueKey);
    }


}
