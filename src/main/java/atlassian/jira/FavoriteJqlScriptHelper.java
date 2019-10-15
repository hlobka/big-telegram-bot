package atlassian.jira;

public class FavoriteJqlScriptHelper {

    private final static String ALL_ISSUES_JQL =                            "project = \"%s\" ORDER BY createdDate DESC";
    private final static String SPRINT_ALL_ISSUES_JQL =                     "project = \"%s\" AND Sprint in openSprints()";
    private final static String SPRINT_CLOSED_ISSUES_JQL =                  "project = \"%s\" AND Sprint in openSprints() AND (status = Closed OR status = Rejected OR status = Verified)";
    private final static String SPRINT_ACTIVE_ISSUES_JQL =                  "project = \"%s\" AND Sprint in openSprints() AND status != Rejected AND status != Closed AND status != Opened AND status != Verified";
    private final static String SPRINT_OPEN_ISSUES_JQL =                    "project = \"%s\" AND Sprint in openSprints() AND status = Opened";
    private final static String SPRINT_UN_ESTIMATED_ISSUES_JQL =            "project = \"%s\" AND Sprint in openSprints() AND originalEstimate is EMPTY";
    private final static String SPRINT_UN_TRACKED_ISSUES_JQL =              "project = \"%s\" AND Sprint in openSprints() AND timespent is EMPTY";
    private final static String SPRINT_CLOSED_AND_UN_TRACKED_ISSUES_JQL =   "project = \"%s\" AND Sprint in openSprints() AND (status = Closed OR status = Rejected OR status = Verified) AND timespent is EMPTY";


    public static String getSprintUnEstimatedIssuesJql(String projectId) {
        return String.format(SPRINT_UN_ESTIMATED_ISSUES_JQL, projectId);
    }

    public static String getSprintUnTrackedIssuesJql(String projectId) {
        return String.format(SPRINT_UN_TRACKED_ISSUES_JQL, projectId);
    }

    public static String getSprintClosedAndUnTrackedIssuesJql(String projectId) {
        return String.format(SPRINT_CLOSED_AND_UN_TRACKED_ISSUES_JQL, projectId);
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

    public static String getAllIssuesJql(String projectId) {
        return String.format(ALL_ISSUES_JQL, projectId);
    }
}
