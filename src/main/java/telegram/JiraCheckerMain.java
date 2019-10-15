package telegram;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import telegram.bot.checker.workFlow.CommonChecker;
import telegram.bot.checker.workFlow.implementations.NewJiraIssuesChecker;
import telegram.bot.checker.workFlow.implementations.UnEstimatedJiraIssuesChecker;
import telegram.bot.checker.workFlow.implementations.UnTrackedJiraIssuesOnReviewChecker;
import telegram.bot.checker.workFlow.implementations.UnTrackedJiraIssuesWhichWasDoneChecker;
import telegram.bot.checker.workFlow.implementations.services.JiraHelperServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.ServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.UpsourceServiceProvider;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JiraCheckerMain {
    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap = new HashMap<>();
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            FavoriteJqlRules jiraConfig = generalChat.getJiraConfig();
            jiraHelperServiceProviderMap.put(jiraConfig.getLoginData().url, new JiraHelperServiceProvider(bot, jiraConfig.getLoginData()));
        }

        UpsourceServiceProvider upsourceServiceProvider = new UpsourceServiceProvider();
        new CommonChecker(bot, TimeUnit.HOURS.toMillis(2))
            .withChecker(new UnTrackedJiraIssuesWhichWasDoneChecker(jiraHelperServiceProviderMap))
            .withIdleTimeoutMultiplier(2)
            .withMaxNumberOfAttempts(5)
            .start();
        new CommonChecker(bot, TimeUnit.MINUTES.toMillis(20))
            .withChecker(new NewJiraIssuesChecker(jiraHelperServiceProviderMap))
            .withChecker(new UnEstimatedJiraIssuesChecker(jiraHelperServiceProviderMap))
            .withChecker(new UnTrackedJiraIssuesOnReviewChecker(jiraHelperServiceProviderMap, upsourceServiceProvider))
            .withIdleTimeoutMultiplier(5)
            .withMaxNumberOfAttempts(5)
            .start();

    }
}
