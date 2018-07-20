package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.log;
import static telegram.bot.data.Common.JIRA_CHECKER_STATUSES;

public class JiraChecker extends Thread {
    private TelegramBot bot;
    private long millis;
    private final JiraHelper jiraHelper;
    private HashMap<String, Integer> statuses;
    private Boolean isFirstTime = true;

    public JiraChecker(TelegramBot bot, long millis) {
        this.bot = bot;
        this.millis = millis;
        jiraHelper = JiraHelper.getClient(Common.JIRA);
        statuses = SharedObject.loadMap(JIRA_CHECKER_STATUSES, new HashMap<>());
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                long millis = isFirstTime ? 1 : this.millis;
                isFirstTime = false;
                TimeUnit.MILLISECONDS.sleep(millis);
                check();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
                return;
            }
        }
    }

    public void check() {
        log("JiraChecker::check:start");
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            check(chatData);
        }
        log("JiraChecker::check:end");

    }

    public void check(ChatData chatData) {
        log("JiraChecker::check:" + chatData.getChatId());
        for (String projectJiraId : chatData.getJiraProjectKeyIds()) {
            Integer lastCreatedIssueId = getIssueId(projectJiraId);
            String message = getAllCreatedIssuesMessage(projectJiraId, lastCreatedIssueId + 1);
            sendMessage(chatData, message);
            statuses.put(projectJiraId, getLastJiraIssueId(projectJiraId, lastCreatedIssueId));
            SharedObject.save(JIRA_CHECKER_STATUSES, statuses);
        }
        log("JiraChecker::check:end");
    }

    private String getAllCreatedIssuesMessage(String projectJiraId, Integer lastCreatedIssueId) {
        String message = "";
        while (jiraHelper.hasIssue(getIssueKey(projectJiraId, lastCreatedIssueId))) {
            String issueKey = getIssueKey(projectJiraId, lastCreatedIssueId);
            String issueDescription = getIssueDescription(issueKey);
            message += issueDescription;
            lastCreatedIssueId++;
        }
        if (message.length() > 0) {
            message = "Обнаружены новые задачи: " + message;
        }
        return message;
    }

    private String getIssueDescription(String issueKey) {
        Issue issue = jiraHelper.getIssue(issueKey);
        String reporter = getName(issue.getReporter());
        String assignee = getName(issue.getAssignee());
        String summary = issue.getSummary();
        Object priority = issue.getPriority() == null ? "Low" : issue.getPriority().getName();
        return String.format("%n%n *[%s]* as: ```%s``` Created by: *%s*,%n Assignee on: *%s* with Priority: * %s *", issueKey, summary, reporter, assignee, priority);
    }

    private String getName(User user) {
        return user == null ? "RIP" : user.getName();
    }

    private String getIssueKey(String projectJiraId, Integer lastCreatedIssueId) {
        return projectJiraId + "-" + lastCreatedIssueId;
    }

    public Integer getIssueId(String projectJiraId) {
        Integer issueId;
        if (!statuses.containsKey(projectJiraId)) {
            issueId = getLastJiraIssueId(projectJiraId);
        } else {
            issueId = statuses.get(projectJiraId);
        }
        return issueId;
    }

    private Integer getLastJiraIssueId(String projectJiraId) {
        return getLastJiraIssueId(projectJiraId, 1);
    }

    private Integer getLastJiraIssueId(String projectJiraId, int previousResult) {
        Integer result = previousResult;
        result = getMaxIssueIdByStep(projectJiraId, result, 100);
        result = getMaxIssueIdByStep(projectJiraId, result, 50);
        result = getMaxIssueIdByStep(projectJiraId, result, 10);
        result = getMaxIssueIdByStep(projectJiraId, result, 5);
        result = getMaxIssueIdByStep(projectJiraId, result, 1);
        return result;
    }

    private int getMaxIssueIdByStep(String projectJiraId, Integer result, int step) {
        while (jiraHelper.hasIssue(getIssueKey(projectJiraId, result))) {
            result += step;
        }
        return result - step;
    }

    private void sendMessage(ChatData chatData, String msg) {
        SendMessage request = new SendMessage(chatData.getChatId(), msg)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(true)
            .disableNotification(false);
        bot.execute(request);
    }
}
