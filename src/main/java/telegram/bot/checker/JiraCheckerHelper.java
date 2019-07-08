package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import telegram.bot.data.Common;

import java.util.List;

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
        String issueKey = issue.getKey();
        String reporter = getName(issue.getReporter());
        String assignee = getName(issue.getAssignee());
        String summary = issue.getSummary();
        Object priority = issue.getPriority() == null ? "Low" : issue.getPriority().getName();
        String linkedIssueKey = JiraCheckerHelper.getTelegramIssueLink(issueKey);
        return String.format("%n%n %s as: ``` %s ``` Created by: *%s*,%n Assignee on: *%s* with Priority: * %s *", linkedIssueKey, summary, reporter, assignee, priority);
    }

    private static String getName(User user) {
        return user == null ? "RIP" : user.getName();
    }

    public String getActiveSprintUnEstimatedIssuesMessage(String projectKey) {
        List<Issue> issues = getActiveSprintUnEstimatedIssues(projectKey);
        StringBuilder result = new StringBuilder("Данные задачи нуждаються в дополнительной экстимации:");
        for (Issue issue : issues) {
            result.append(getIssueDescription(issue));
        }
        return result.toString();
    }

    public List<Issue> getActiveSprintUnEstimatedIssues(String projectKey) {
        return jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintUnEstimatedIssuesJql(projectKey));
    }

    public static String getTelegramIssueLink(String issueKey) {
        return getTelegramIssueLink(issueKey, Common.JIRA.url);
    }

    public static String getTelegramIssueLink(String issueKey, String jiraUrl) {
        return String.format("[%1$s](%2$s/browse/%1$s)", issueKey, jiraUrl);
    }


}
