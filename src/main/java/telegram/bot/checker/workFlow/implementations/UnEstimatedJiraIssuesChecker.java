package telegram.bot.checker.workFlow.implementations;

import atlassian.jira.JiraHelper;
import helper.file.SharedObject;
import telegram.bot.checker.JiraCheckerHelper;
import telegram.bot.checker.workFlow.ChatChecker;
import telegram.bot.checker.workFlow.implementations.services.JiraHelperServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.UpsourceServiceProvider;
import telegram.bot.data.chat.ChatData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.JIRA_CHECKER_STATUSES;

public class UnEstimatedJiraIssuesChecker implements ChatChecker {
    private final JiraHelperServiceProvider jiraHelperServiceProvider;

    public UnEstimatedJiraIssuesChecker(JiraHelperServiceProvider jiraHelperServiceProvider) {
        this.jiraHelperServiceProvider = jiraHelperServiceProvider;
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
        jiraHelperServiceProvider.provide(jiraHelper -> {
            JiraCheckerHelper jiraCheckerHelper = new JiraCheckerHelper(jiraHelper);
            for (String projectJiraId : chatData.getJiraProjectKeyIds()) {
                logFor(this, String.format("check:%s[%s]", chatData.getChatId(), projectJiraId));
                Boolean excludeBugs = chatData.getIsEstimationRequiredExcludeBugs();
                String message = jiraCheckerHelper.getActiveSprintUnEstimatedIssuesMessage(projectJiraId, excludeBugs);
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
