package telegram.bot.commands;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import javafx.util.Pair;
import telegram.bot.checker.JiraBigMetricsCollector;
import telegram.bot.checker.JiraBigMetricsProvider;
import telegram.bot.data.Common;
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
        if (callbackQuery != null){
            Message message = callbackQuery.message();
            if (message != null) {
                try {
                    String metrics = getMetrics(jiraId);
                    BotHelper.sendMessage(bot, message.chat().id(), metrics, ParseMode.Markdown);
                } catch (RuntimeException e){
                    ConsoleLogger.logErrorFor(this, e);
                    throw e;
                }
            }
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Ok: "));
    }

    public String getMetrics(String jiraId) {
        String metrics = jiraId + " metrics:\n";
        JiraHelper jiraHelper = JiraHelper.tryToGetClient(Common.JIRA, true, e -> ReLoginRule.tryToRelogin(bot, e));
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
        return metrics;
    }
}
