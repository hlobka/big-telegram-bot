package atlassian.jira;

public class FavoriteJqlScriptHelper {

    private final static String SPRINT_ALL_ISSUES_JQL = "project = %s AND Sprint in openSprints()";
    private final static String SPRINT_CLOSED_ISSUES_JQL = "project = %s AND Sprint in openSprints() AND (status = Closed OR status = Rejected)";
    private final static String SPRINT_ACTIVE_ISSUES_JQL = "project = %s AND Sprint in openSprints() AND status != Rejected AND status != Closed AND status != Opened";
    private final static String SPRINT_OPEN_ISSUES_JQL = "project = %s AND Sprint in openSprints() AND status = Opened";
    private final static String SPRINT_UN_ESTIMATED_ISSUES_JQL = "project = %s AND Sprint in openSprints() AND originalEstimate is EMPTY";


    public static String getSprintUnEstimatedIssuesJql(String projectId) {
        return String.format(SPRINT_UN_ESTIMATED_ISSUES_JQL, projectId);
    }

    public static String getSprintAllIssuesJql(String projectId) {
        return String.format(SPRINT_ALL_ISSUES_JQL, projectId);
    }

    public static String getSprintClosedIssuesJql(String projectId) {
        return String.format(SPRINT_CLOSED_ISSUES_JQL, projectId);
    }

    public static String getSprintActiveIssuesJql(String projectId) {
        return String.format(SPRINT_ACTIVE_ISSUES_JQL, projectId);
    }

    public static String getSprintOpenIssuesJql(String projectId) {
        return String.format(SPRINT_OPEN_ISSUES_JQL, projectId);
    }
}
