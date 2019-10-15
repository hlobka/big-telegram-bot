package telegram;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;

public class JiraRestClientMain {

    public static void main(String[] args) {
        LoginData jiraLoginData = Common.data.getChatData("REPORT").getJiraConfig().getLoginData();
        JiraHelper jiraHelper = JiraHelper.getClient(jiraLoginData);
        Issue issue = jiraHelper.getIssue("WILDFU-287");
        System.out.println("Summary = " + issue.getSummary() + ", Status = " + (issue.getStatus() != null ? issue.getStatus().getName() : "N/A"));
    }
}
