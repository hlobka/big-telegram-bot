package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.rules.ReLoginRule;

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.JIRA_CHECKER_STATUSES;

public class JiraChecker extends Thread {
    public static final int MAX_ISSUES_ON_ONE_POST = 10;
    private TelegramBot bot;
    private long millis;
    private final JiraHelper jiraHelper;
    private HashMap<String, Integer> statuses;
    private Boolean isFirstTime = true;

    public JiraChecker(TelegramBot bot, long millis) {
        this.bot = bot;
        this.millis = millis;
        jiraHelper = JiraHelper.tryToGetClient(Common.JIRA, true, e -> ReLoginRule.tryToRelogin(bot, e));
        statuses = SharedObject.loadMap(JIRA_CHECKER_STATUSES, new HashMap<>());
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                if (!sleepToNextCheck()) {
                    continue;
                }
                check();
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
                Thread.interrupted();
                return;
            }
        }
    }

    public boolean sleepToNextCheck() throws InterruptedException {
        long millis = isFirstTime ? 1 : this.millis;
        isFirstTime = false;
        logFor(this, "sleepToNextCheck: " + TimeUnit.MILLISECONDS.toMinutes(millis) + " minutes");
        TimeUnit.MILLISECONDS.sleep(millis);
        if (TimeHelper.isWeekends() || TimeHelper.isNight()) {
            logFor(this, "sleepToNextCheck: 10 minutes");
            TimeUnit.MINUTES.sleep(10);
            return false;
        }
        return true;
    }

    public void check() {
        logFor(this, "check:start");
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            check(chatData);
        }
        logFor(this, "check:end");

    }

    public void check(ChatData chatData) {
        for (String projectJiraId : chatData.getJiraProjectKeyIds()) {
            logFor(this, String.format("check:%s[%s]", chatData.getChatId(), projectJiraId));
            Integer lastCreatedOrPostedIssueId = getIssueId(projectJiraId);
            sendAllMessages(chatData, projectJiraId, lastCreatedOrPostedIssueId);
            Integer lastJiraIssueId = getLastJiraIssueId(projectJiraId, lastCreatedOrPostedIssueId);
            statuses.put(projectJiraId, lastJiraIssueId);
            SharedObject.save(JIRA_CHECKER_STATUSES, statuses);
            logFor(this, String.format("check:posted %s: issues id from: %d to: %d", projectJiraId, lastCreatedOrPostedIssueId, lastJiraIssueId));
            logFor(this, String.format("check:%s[%s]:end", chatData.getChatId(), projectJiraId));
        }
    }

    public void sendAllMessages(ChatData chatData, String projectJiraId, Integer lastPostedIssueId) {
        while (hasIssuesInDiapason(projectJiraId, lastPostedIssueId, lastPostedIssueId + MAX_ISSUES_ON_ONE_POST)) {
            String message = getAllCreatedIssuesMessage(projectJiraId, lastPostedIssueId + 1);
            sendMessage(chatData, message);
            lastPostedIssueId += MAX_ISSUES_ON_ONE_POST;
        }
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
            if (jiraHelper.hasIssue(getIssueKey(projectJiraId, lastCreatedIssueId))) {
                String issueKey = getIssueKey(projectJiraId, lastCreatedIssueId);
                String issueDescription = getIssueDescription(issueKey);
                message += issueDescription;
            }
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
        String linkedIssueKey = String.format("[%1$s](%2$s/browse/%1$s)", issueKey, Common.JIRA.url);
        return String.format("%n%n %s as: ``` %s ``` Created by: *%s*,%n Assignee on: *%s* with Priority: * %s *", linkedIssueKey, summary, reporter, assignee, priority);
    }

    private String getName(User user) {
        return user == null ? "RIP" : user.getName();
    }

    private String getIssueKey(String projectJiraId, Integer lastCreatedIssueId) {
        return projectJiraId + "-" + lastCreatedIssueId;
    }

    private Integer getIssueId(String projectJiraId) {
        Integer issueId;
        if (!statuses.containsKey(projectJiraId)) {
            issueId = getLastJiraIssueId(projectJiraId);
        } else {
            issueId = statuses.get(projectJiraId);
        }
        issueId = Math.max(0, issueId);
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
        result = getMaxIssueIdByStep(projectJiraId, result, 3);
        result = getMaxIssueIdByStep(projectJiraId, result, 2);
        result = getMaxIssueIdByStep(projectJiraId, result, 1);
        return result;
    }

    private int getMaxIssueIdByStep(String projectJiraId, Integer issueNumber, int step) {
        Integer result = issueNumber;
        while (jiraHelper.hasIssue(getIssueKey(projectJiraId, result + step))) {
            result += step;
        }
        return result;
    }

    private void sendMessage(ChatData chatData, String msg) {
        SendMessage request = new SendMessage(chatData.getChatId(), msg)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .disableNotification(false);
        bot.execute(request);
    }
}
