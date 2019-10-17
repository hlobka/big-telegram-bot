package telegram.bot.data.jira;

import lombok.Data;
import telegram.bot.data.LoginData;

@Data
public class FavoriteJqlRules {
    private final String allIssuesJql;
    private final String allEstimatedOrTrackedIssues;
    private final String allClosedAndEstimatedOrTrackedIssues;
    private final String sprintAllIssuesJql;
    private final String sprintClosedIssuesJql;
    private final String sprintActiveIssuesJql;
    private final String sprintOpenIssuesJql;
    private final String sprintUnEstimatedIssuesJql;
    private final String sprintUnTrackedIssuesJql;
    private final String sprintClosedAndUnTrackedIssuesJql;
    private final LoginData loginData;

    public String getAllIssuesJql(String projectKey) {
        return String.format(getAllIssuesJql(), projectKey);
    }

    public String getAllEstimatedOrTrackedIssues(String projectKey) {
        return String.format(getAllEstimatedOrTrackedIssues(), projectKey);
    }

    public String getAllClosedAndEstimatedOrTrackedIssues(String projectKey) {
        return String.format(getAllClosedAndEstimatedOrTrackedIssues(), projectKey);
    }

    public String getSprintAllIssuesJql(String projectKey) {
        return String.format(getSprintAllIssuesJql(), projectKey);
    }

    public String getSprintClosedIssuesJql(String projectKey) {
        return String.format(getSprintClosedIssuesJql(), projectKey);
    }

    public String getSprintActiveIssuesJql(String projectKey) {
        return String.format(getSprintActiveIssuesJql(), projectKey);
    }

    public String getSprintOpenIssuesJql(String projectKey) {
        return String.format(getSprintOpenIssuesJql(), projectKey);
    }

    public String getSprintUnEstimatedIssuesJql(String projectKey) {
        return String.format(getSprintUnEstimatedIssuesJql(), projectKey);
    }

    public String getSprintUnTrackedIssuesJql(String projectKey) {
        return String.format(getSprintUnTrackedIssuesJql(), projectKey);
    }

    public String getSprintClosedAndUnTrackedIssuesJql(String projectKey) {
        return String.format(getSprintClosedAndUnTrackedIssuesJql(), projectKey);
    }
}
