package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.ReLoginRule;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JiraSprintBarShower extends Thread {

    private final static Integer RULE_WIDTH = 12;
    private final static String SPRINT_ALL_ISSUES_JQL       = "project = %s AND Sprint in openSprints()";
    private final static String SPRINT_CLOSED_ISSUES_JQL    = "project = %s AND Sprint in openSprints() AND (status = Closed OR status = Rejected)";
    private final static String SPRINT_ACTIVE_ISSUES_JQL    = "project = %s AND Sprint in openSprints() AND status != Rejected AND status != Closed AND status != Opened";
    private final static String SPRINT_OPEN_ISSUES_JQL      = "project = %s AND Sprint in openSprints() AND status = Opened";
    private TelegramBot bot;
    private long millis;
    private final JiraHelper jiraHelper;
    private Boolean isFirstTime = true;

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
//        ChatData chatData = Common.data.getChatData("REPORT");
        JiraSprintBarShower jiraSprintBarShower = new JiraSprintBarShower(bot, TimeUnit.MINUTES.toMillis(60));
        jiraSprintBarShower.show("FOREGY");
        jiraSprintBarShower.show("SPHICL");
        jiraSprintBarShower.show("BOOSPH");
        jiraSprintBarShower.show("MAGOIFX");
        jiraSprintBarShower.show("FBIXF");
        jiraSprintBarShower.show("TRH");
    }

    public JiraSprintBarShower(TelegramBot bot, long millis) {
        this.bot = bot;
        this.millis = millis;
        jiraHelper = JiraHelper.tryToGetClient(Common.JIRA, true, e -> ReLoginRule.tryToRelogin(bot, e));
    }

    @Override
    public void run() {
        super.run();
        if (isFirstTime) {
            isFirstTime = false;
        } else {
        }
        show("FOREGY");
        while (true){
            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
                ConsoleLogger.logErrorFor(this, e);
                Thread.interrupted();
                return;
            }
            show("FOREGY");
        }
    }

    private void show(String projectKey) {
        int closedIssuesAmount = jiraHelper.getIssues(String.format(SPRINT_CLOSED_ISSUES_JQL, projectKey)).size();
        int openedIssuesAmount = jiraHelper.getIssues(String.format(SPRINT_ACTIVE_ISSUES_JQL, projectKey)).size();
        int activeIssuesAmount = jiraHelper.getIssues(String.format(SPRINT_OPEN_ISSUES_JQL, projectKey)).size();
        sendMessage(closedIssuesAmount, openedIssuesAmount, activeIssuesAmount);
    }

    private void sendMessage(int fullClosedIssuesAmount, int fullOpenedIssuesAmount, int fullActiveIssuesAmount) {
        String message = StringHelper.getBar(
            Stream.of(fullOpenedIssuesAmount, fullActiveIssuesAmount, fullClosedIssuesAmount).map(Double::valueOf).collect(Collectors.toList()),
            Arrays.asList("⚪", "🔵", "🔴"),
            RULE_WIDTH
        );
        ConsoleLogger.log(message);
        long chatId = Common.data.getChatForReport().get(0).getChatId();
        BotHelper.sendMessage(bot, chatId, message , ParseMode.Markdown);
    }
}
