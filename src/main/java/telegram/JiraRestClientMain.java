package telegram;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import telegram.bot.data.Common;

public class JiraRestClientMain {

    public static void main(String[] args) {
        JiraHelper jiraHelper = JiraHelper.getClient(Common.JIRA);
        Issue issue = jiraHelper.getIssue("WILDFU-287");
        System.out.println("Summary = " + issue.getSummary() + ", Status = " + (issue.getStatus() != null ? issue.getStatus().getName() : "N/A"));
    }
}
