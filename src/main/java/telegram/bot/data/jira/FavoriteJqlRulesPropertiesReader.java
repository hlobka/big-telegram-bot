package telegram.bot.data.jira;

import telegram.bot.data.LoginData;

import java.util.Properties;

public class FavoriteJqlRulesPropertiesReader {
    private final Properties properties;

    public FavoriteJqlRulesPropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public String getAllIssuesJql() {
        return properties.getProperty("rules.jql.getAllIssues");
    }

    public String getAllEstimatedOrTrackedIssues() {
        return properties.getProperty("rules.jql.getAllEstimatedOrTrackedIssues");
    }

    public String getAllClosedAndEstimatedOrTrackedIssues() {
        return properties.getProperty("rules.jql.getAllClosedAndEstimatedOrTrackedIssues");
    }

    public String getSprintAllIssuesJql() {
        return properties.getProperty("rules.jql.getSprintAllIssues");
    }

    public String getSprintClosedIssuesJql() {
        return properties.getProperty("rules.jql.getSprintClosedIssues");
    }

    public String getSprintActiveIssuesJql() {
        return properties.getProperty("rules.jql.getSprintActiveIssues");
    }

    public String getSprintOpenIssuesJql() {
        return properties.getProperty("rules.jql.getSprintOpenIssues");
    }

    public String getSprintUnEstimatedIssuesJql() {
        return properties.getProperty("rules.jql.getSprintUnEstimatedIssues");
    }

    public String getSprintUnTrackedIssuesJql() {
        return properties.getProperty("rules.jql.getSprintUnTrackedIssues");
    }

    public String getSprintClosedAndUnTrackedIssuesJql() {
        return properties.getProperty("rules.jql.getSprintClosedAndUnTrackedIssues");
    }

    public LoginData getLoginData() {
        return new LoginData(properties, "jira");
    }
}
