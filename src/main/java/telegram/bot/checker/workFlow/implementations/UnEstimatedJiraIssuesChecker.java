package telegram.bot.checker.workFlow.implementations;

import atlassian.jira.JiraHelper;
import helper.file.SharedObject;
import telegram.bot.checker.JiraCheckerHelper;
import telegram.bot.checker.workFlow.ChatChecker;
import telegram.bot.checker.workFlow.implementations.services.ServiceProvider;
import telegram.bot.data.chat.ChatData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.JIRA_CHECKER_STATUSES;

public class UnEstimatedJiraIssuesChecker implements ChatChecker {
    private final Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap;

    public UnEstimatedJiraIssuesChecker(Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap) {
        this.jiraHelperServiceProviderMap = jiraHelperServiceProviderMap;
    }

    @Override
    public Boolean isAccessibleToCheck(ChatData chatData) {
        return !chatData.getJiraProjectKeyIds().isEmpty() && chatData.getIsEstimationRequired();
    }

    @Override
    public List<String> check(ChatData chatData) {
        logFor(this, "check:start");
        List<String> result = new ArrayList<>();
        HashMap<String, Integer> statuses = SharedObject.loadMap(JIRA_CHECKER_STATUSES, new HashMap<>());
        String jiraUrl = chatData.getJiraConfig().getLoginData().url;
        ServiceProvider<JiraHelper> jiraHelperServiceProvider = jiraHelperServiceProviderMap.get(jiraUrl);
        jiraHelperServiceProvider.provide(jiraHelper -> {
            JiraCheckerHelper jiraCheckerHelper = new JiraCheckerHelper(jiraHelper);
            for (String projectJiraId : chatData.getJiraProjectKeyIds()) {
                logFor(this, String.format("check:%s[%s]", chatData.getChatId(), projectJiraId));
                String message = jiraCheckerHelper.getActiveSprintUnEstimatedIssuesMessage(projectJiraId, chatData.getJiraConfig());
                if (!message.isEmpty()) {
                    result.add(message);
                }
                logFor(this, String.format("check:%s[%s]:end", chatData.getChatId(), projectJiraId));
            }
        });
        logFor(this, "check:end");
        return result;
    }
}
