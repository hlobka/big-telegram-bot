package telegram.bot.commands;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.JiraBigMetricsCollector;
import telegram.bot.checker.JiraBigMetricsProvider;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.ReLoginRule;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShowJiraMetricsByProjectIdCommand implements Command {
    private TelegramBot bot;

    public ShowJiraMetricsByProjectIdCommand(TelegramBot bot) {
        this.bot = bot;
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
        String metrics = jiraId + " metrics:\n";
        final LoginData loginData = getLoginData(jiraId);
        if (loginData == null) {
            return "No available login data's for: " + jiraId;
        }
        JiraHelper jiraHelper = JiraHelper.tryToGetClient(loginData, true, e -> ReLoginRule.tryToRelogin(bot, e, loginData));
        try {
            JiraBigMetricsCollector jiraBigMetricsCollector = new JiraBigMetricsCollector(jiraHelper, jiraId);
            JiraBigMetricsProvider jiraBigMetricsProvider = jiraBigMetricsCollector.collect(TimeUnit.HOURS);
            metrics += "\nPV:  " + jiraBigMetricsProvider.getPlannedValue();
            metrics += "\nEV:  " + jiraBigMetricsProvider.getEarnedValue();
            metrics += "\nAC:  " + jiraBigMetricsProvider.getActualCost();
            metrics += "\nSV:  " + jiraBigMetricsProvider.getScheduleVariance();
            metrics += "\nSPI: " + jiraBigMetricsProvider.getSchedulePerformanceIndex();
            metrics += "\nCV:  " + jiraBigMetricsProvider.getCostVariance();
            metrics += "\nCPI: " + jiraBigMetricsProvider.getCostPerformanceIndex();
            metrics += "\nBAC: " + jiraBigMetricsProvider.getBudgetAtCompletion();
            metrics += "\nEAC: " + jiraBigMetricsProvider.getEstimateAtCompletion();
            metrics += "\nETC: " + jiraBigMetricsProvider.getEstimateToComplete();
            metrics += "\nVAC: " + jiraBigMetricsProvider.getVarianceAtCompletion();
        } finally {
            jiraHelper.disconnect();
        }
        return metrics;
    }

    private LoginData getLoginData(String jiraId) {
        LoginData loginData = null;
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            if (generalChat.getJiraProjectKeyIds().contains(jiraId)) {
                loginData = generalChat.getJiraLoginData();
            }
        }
        return loginData;
    }
}
