package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.ReLoginRule;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JiraSprintBarShower extends Thread {

    private final static Integer RULE_WIDTH = 12;
    private TelegramBot bot;
    private long millis;
    private final JiraHelper jiraHelper;
    private Boolean isFirstTime = true;

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        ChatData chatData = Common.data.getChatData("REPORT");
        LoginData jiraLoginData = chatData.getJiraConfig().getLoginData();
        JiraHelper jiraHelper = JiraHelper.tryToGetClient(jiraLoginData, true, e -> ReLoginRule.tryToRelogin(bot, e, jiraLoginData));
        JiraSprintBarShower jiraSprintBarShower = new JiraSprintBarShower(jiraHelper, bot, TimeUnit.MINUTES.toMillis(60));
        jiraSprintBarShower.show("FOREGY");
        jiraSprintBarShower.show("SPHICL");
        jiraSprintBarShower.show("BOOSPH");
        jiraSprintBarShower.show("MAGOIFX");
        jiraSprintBarShower.show("FBIXF");
        jiraSprintBarShower.show("TRH");
    }

    public JiraSprintBarShower(JiraHelper jiraHelper, TelegramBot bot, long millis) {
        this.jiraHelper = jiraHelper;
        this.bot = bot;
        this.millis = millis;
    }

    @Override
    public void run() {
        super.run();
        if (isFirstTime) {
            isFirstTime = false;
        } else {
        }
        show("FOREGY");
        while (true) {
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
        int closedIssuesAmount = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintClosedIssuesJql(projectKey)).size();
        int openedIssuesAmount = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintActiveIssuesJql(projectKey)).size();
        int activeIssuesAmount = jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintOpenIssuesJql(projectKey)).size();
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
        BotHelper.sendMessage(bot, chatId, message, ParseMode.Markdown);
    }
}
