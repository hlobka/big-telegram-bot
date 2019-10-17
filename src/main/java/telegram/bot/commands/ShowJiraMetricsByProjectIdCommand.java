package telegram.bot.commands;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.data.jira.FavoriteJqlRules;
import telegram.bot.helper.BotHelper;
import telegram.bot.metrics.jira.JiraAllPeriodMetricsCollector;
import telegram.bot.metrics.jira.JiraMetricsCollector;
import telegram.bot.metrics.jira.JiraMetricsProvider;
import telegram.bot.metrics.jira.JiraSprintMetricsCollector;
import telegram.bot.rules.ReLoginRule;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShowJiraMetricsByProjectIdCommand implements Command {
    private TelegramBot bot;
    private boolean forAllPeriod;

    public ShowJiraMetricsByProjectIdCommand(TelegramBot bot, boolean forAllPeriod) {
        this.bot = bot;
        this.forAllPeriod = forAllPeriod;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String jiraId) {
        CallbackQuery callbackQuery = update.callbackQuery();
        if (callbackQuery != null) {
            Message message = callbackQuery.message();
            if (message != null) {
                try {
                    String metrics = getMetrics(jiraId);
                    BotHelper.sendMessage(bot, message.chat().id(), metrics, ParseMode.Markdown);
                } catch (RuntimeException e) {
                    BotHelper.sendMessage(bot, message.chat().id(), e.getMessage(), ParseMode.Markdown);
                    e.printStackTrace();
                }
            }
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Ok: "));
    }

    private String getMetrics(String jiraId) {
        String metrics = (forAllPeriod ? "Full" : "Sprint") + " project metrics for " + jiraId + " metrics:\n";
        final FavoriteJqlRules jiraConfig = getJiraConfig(jiraId);
        if (jiraConfig == null) {
            return "No available login data's for: " + jiraId;
        }
        LoginData loginData = jiraConfig.getLoginData();
        JiraHelper jiraHelper = JiraHelper.tryToGetClient(loginData, true, e -> ReLoginRule.tryToRelogin(bot, e, loginData));
        try {
            JiraMetricsCollector jiraMetricsCollector;
            if (forAllPeriod) {
                jiraMetricsCollector = new JiraAllPeriodMetricsCollector(jiraHelper, jiraConfig, jiraId);
            } else {
                jiraMetricsCollector = new JiraSprintMetricsCollector(jiraHelper, jiraConfig, jiraId);
            }
            JiraMetricsProvider jiraMetricsProvider = jiraMetricsCollector.collect(TimeUnit.HOURS);
            metrics += "\nPV:  " + jiraMetricsProvider.getPlannedValue();
            metrics += "\nEV:  " + jiraMetricsProvider.getEarnedValue();
            metrics += "\nAC:  " + jiraMetricsProvider.getActualCost();
            metrics += "\nSV:  " + jiraMetricsProvider.getScheduleVariance();
            metrics += "\nSPI: " + jiraMetricsProvider.getSchedulePerformanceIndex();
            metrics += "\nCV:  " + jiraMetricsProvider.getCostVariance();
            metrics += "\nCPI: " + jiraMetricsProvider.getCostPerformanceIndex();
            metrics += "\nBAC: " + jiraMetricsProvider.getBudgetAtCompletion();
            metrics += "\nEAC: " + jiraMetricsProvider.getEstimateAtCompletion();
            metrics += "\nETC: " + jiraMetricsProvider.getEstimateToComplete();
            metrics += "\nVAC: " + jiraMetricsProvider.getVarianceAtCompletion();
        } finally {
            jiraHelper.disconnect();
        }
        return metrics;
    }

    private FavoriteJqlRules getJiraConfig(String jiraId) {
        FavoriteJqlRules jiraConfig = null;
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            if (generalChat.getJiraProjectKeyIds().contains(jiraId)) {
                jiraConfig = generalChat.getJiraConfig();
            }
        }
        return jiraConfig;
    }
}
