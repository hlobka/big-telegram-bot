package telegram.bot.checker.workFlow.implementations;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import helper.file.SharedObject;
import helper.string.StringHelper;
import telegram.bot.checker.JiraCheckerHelper;
import telegram.bot.checker.workFlow.ChatChecker;
import telegram.bot.checker.workFlow.implementations.services.ServiceProvider;
import telegram.bot.data.chat.ChatData;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.JIRA_CHECKER_STATUSES;

public class NewJiraIssuesChecker implements ChatChecker {
    public static final int MAX_ISSUES_ON_ONE_POST = 10;
    public static final int MAX_ISSUES_AMOUNT = 100;
    private final Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap;

    private HashMap<String, Integer> statuses;
    private JiraHelper jiraHelper;

    public NewJiraIssuesChecker(Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap) {
        this.jiraHelperServiceProviderMap = jiraHelperServiceProviderMap;
        statuses = SharedObject.loadMap(JIRA_CHECKER_STATUSES, new HashMap<>());
    }

    @Override
    public Boolean isAccessibleToCheck(ChatData chatData) {
        return !chatData.getJiraProjectKeyIds().isEmpty();
    }

    @Override
    public List<String> check(ChatData chatData) {
        logFor(this, "check:start");
        List<String> result = new ArrayList<>();
        String jiraUrl = chatData.getJiraConfig().getLoginData().url;
        ServiceProvider<JiraHelper> jiraHelperServiceProvider = jiraHelperServiceProviderMap.get(jiraUrl);
        jiraHelperServiceProvider.provide(jiraHelper1 -> {
            jiraHelper = jiraHelper1;
            for (String projectJiraId : chatData.getJiraProjectKeyIds()) {
                logFor(this, String.format("check:%s[%s]", chatData.getChatId(), projectJiraId));
                Integer lastCreatedOrPostedIssueId = getIssueId(projectJiraId);
                result.addAll(getAllMessages(projectJiraId, lastCreatedOrPostedIssueId));
                Integer lastJiraIssueId = getLastJiraIssueId(projectJiraId, chatData.getJiraConfig());
                statuses.put(projectJiraId, lastJiraIssueId);
                SharedObject.save(JIRA_CHECKER_STATUSES, statuses);
                logFor(this, String.format("check:posted %s: issues id from: %d to: %d", projectJiraId, lastCreatedOrPostedIssueId, lastJiraIssueId));
                logFor(this, String.format("check:%s[%s]:end", chatData.getChatId(), projectJiraId));
            }
        });
        logFor(this, "check:end");
        return result;
    }

    public List<String> getAllMessages(String projectJiraId, Integer lastPostedIssueId) {
        List<String> result = new ArrayList<>();
        while (hasIssuesInDiapason(projectJiraId, lastPostedIssueId, lastPostedIssueId + MAX_ISSUES_ON_ONE_POST)) {
            result.add(getAllCreatedIssuesMessage(projectJiraId, lastPostedIssueId + 1));
            lastPostedIssueId += MAX_ISSUES_ON_ONE_POST;
        }
        return result;
    }

    private Boolean hasIssuesInDiapason(String projectJiraId, Integer issueIdFrom, Integer issueIdTo) {
        for (int i = issueIdFrom; i < issueIdTo; i++) {
            if (jiraHelper.hasIssue(getIssueKey(projectJiraId, i))) {
                return true;
            }
        }
        return false;
    }

    private String getAllCreatedIssuesMessage(String projectJiraId, Integer lastCreatedIssueId) {
        String message = "";
        for (int i = 0; i < MAX_ISSUES_ON_ONE_POST; i++) {
            String issueKey = getIssueKey(projectJiraId, lastCreatedIssueId);
            if (jiraHelper.hasIssue(issueKey)&& jiraHelper.getIssue(issueKey).getKey().equalsIgnoreCase(issueKey)) {
                String issueDescription = JiraCheckerHelper.getIssueDescription(jiraHelper.getIssue(issueKey));
                message += issueDescription;
            }
            lastCreatedIssueId++;
        }
        if (message.length() > 0) {
            message = "Обнаружены новые задачи: " + message;
        }
        return message;
    }

    private String getIssueKey(String projectJiraId, Integer lastCreatedIssueId) {
        return projectJiraId + "-" + lastCreatedIssueId;
    }

    private Integer getIssueId(String projectJiraId) {
        Integer issueId = 0;
        if (!statuses.containsKey(projectJiraId)) {
            int maxIssuesAmount = MAX_ISSUES_AMOUNT;
            while (!jiraHelper.hasIssue(getIssueKey(projectJiraId, ++issueId))) {
                if (maxIssuesAmount-- < 0) {
                    throw new RuntimeException(String.format("MAX_ISSUES_AMOUNT reached: %d", MAX_ISSUES_AMOUNT));
                }
            }
        } else {
            issueId = statuses.get(projectJiraId);
        }
        issueId = Math.max(0, issueId);
        return issueId;
    }

    private Integer getLastJiraIssueId(String projectJiraId, FavoriteJqlRules jiraConfig) {
        Issue issue = jiraHelper.getIssues(jiraConfig.getAllIssuesJql(projectJiraId), 1, 0).get(0);
        return Integer.valueOf(StringHelper.getRegString(issue.getKey(), "\\w+-(\\d+)", 1));
    }
}
