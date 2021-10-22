package telegram.bot;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
//import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import helper.logger.ConsoleLogger;
import okhttp3.OkHttpClient;
import telegram.bot.checker.*;
import telegram.bot.checker.workFlow.CommonChecker;
import telegram.bot.checker.workFlow.CommonCheckerWithRemoveOfPreviousMessages;
import telegram.bot.checker.workFlow.implementations.NewJiraIssuesChecker;
import telegram.bot.checker.workFlow.implementations.UnEstimatedJiraIssuesChecker;
import telegram.bot.checker.workFlow.implementations.UnTrackedJiraIssuesOnReviewChecker;
import telegram.bot.checker.workFlow.implementations.UnTrackedJiraIssuesWhichWasDoneChecker;
import telegram.bot.checker.workFlow.implementations.services.JiraHelperServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.ServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.UpsourceServiceProvider;
import telegram.bot.commands.*;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.*;
import telegram.bot.rules.like.LikeAnswerRule;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainBot {
    public static void main(String[] args) throws URISyntaxException {
        String jenkinsProxyHost = Common.JENKINS_PROXY_HOST;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(jenkinsProxyHost, Integer.parseInt(Common.JENKINS_PROXY_PORT)));
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
//            .proxy(proxy)
            .build();
//        TelegramBot bot = TelegramBotAdapter.buildCustom(Common.data.token, client);
        TelegramBot bot = new TelegramBot(Common.data.token);
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        System.out.println("onResponse: " + updates.toString());
        Rules rules = new Rules();
        rules.registerRule(new ClearRedundantMessagesRule(bot));
        rules.registerRule(new SlotMachineRule(bot));
        rules.registerRule(new AnswerRule(bot));
        rules.registerRule(new IIAnswerRule(bot));
        rules.registerRule(new EtsAnswerRule(bot));
        rules.registerRule(new LikeAnswerRule(bot));
        rules.registerRule(new BotSayAnswerRule(bot));
        rules.registerRule(new ReLoginRule(bot));
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(bot);
        commandExecutorRule.addCallBackCommand("update_upsource_checker_view_result_for", new UpdateUpsourceViewResult(bot));
        commandExecutorRule.addCallBackCommand(ShowJiraMetricsCommand.SHOW_JIRA_STATISTIC, new ShowJiraMetricsByProjectIdCommand(bot, false));
        commandExecutorRule.addCallBackCommand(ShowJiraMetricsCommand.SHOW_JIRA_STATISTIC_FOR_ALL_PERIOD, new ShowJiraMetricsByProjectIdCommand(bot, true));
        commandExecutorRule.addCallBackCommand("show_upsource_checker_tabs_description", new ShowAlertFromResource(Common.UPSOURCE.checkerHelpLink, bot));
        commandExecutorRule.addCallBackCommand("show_upsource_checker_possible_problems", new ShowAlertFromResource(Common.UPSOURCE.checkerPossibleProblemsHelpLink, bot));
        commandExecutorRule.addCommand("/get_chat_id", new GetChatIdCommand());
        commandExecutorRule.addCommand("/get_user_id_by_name", new GetUserIdByNameCommand());
        commandExecutorRule.addCommand("/get_user_id", new GetUserIdByReplyCommand());
        commandExecutorRule.addCommand("/remove_user_from_ets_list", new RemoveUserFromEtsListByReplyCommand(bot));
        commandExecutorRule.addCommand("/send_on_vacation_by_id", new AddUserByIdOnVacationListCommand(bot));
        commandExecutorRule.addCommand("/send_on_vacation", new SendUserOnVacationByReplyListCommand(bot));
        commandExecutorRule.addCommand("/return_from_vacation_by_id", new RemoveUserFromVacationListCommand(bot));
        commandExecutorRule.addCommand("/return_from_vacation", new RemoveUserFromVacationListCommand(bot));
        commandExecutorRule.addCommand("/configureActionItems", new ConfigureActionItems(false));
        commandExecutorRule.addCommand("/configure_Action_Items", new ConfigureActionItems(false));
        commandExecutorRule.addCommand("/configureAllActionItems", new ConfigureActionItems(true));
        commandExecutorRule.addCommand("/configure_All_Action_Items", new ConfigureActionItems(true));
        commandExecutorRule.addCommand("/showResolvedActionItems", new ShowResolvedActionItems(false));
        commandExecutorRule.addCommand("/show_Resolved_Action_Items", new ShowResolvedActionItems(false));
        commandExecutorRule.addCommand("/showAllResolvedActionItems", new ShowResolvedActionItems(true));
        commandExecutorRule.addCommand("/show_All_Resolved_Action_Items", new ShowResolvedActionItems(true));
        commandExecutorRule.addCommand("/showActionItems", new ShowActionItems(false));
        commandExecutorRule.addCommand("/show_Action_Items", new ShowActionItems(false));
        commandExecutorRule.addCommand("/showAllActionItems", new ShowActionItems(true));
        commandExecutorRule.addCommand("/show_All_Action_Items", new ShowActionItems(true));
        commandExecutorRule.addCommand("/resolveAI", new ClearActionItem());
        commandExecutorRule.addCommand("/resolve_AI", new ClearActionItem());
        commandExecutorRule.addCommand("/help", new ShowHelp());
        commandExecutorRule.addCommand("/showHelpLinks", new ShowHelpLinks());
        commandExecutorRule.addCommand("/show_help_links", new ShowHelpLinks());
        commandExecutorRule.addCommand("/resolve_ets", new ResolveEts(bot));
        commandExecutorRule.addCommand("/show_reviews", new ShowUpsourceReviewCommand(bot));
        commandExecutorRule.addCommand("/show_ets", new ShowEtsCommand(bot));
        commandExecutorRule.addCommand("/show_sprint_jira_metrics", new ShowJiraMetricsCommand(bot, ShowJiraMetricsCommand.SHOW_JIRA_STATISTIC));
        commandExecutorRule.addCommand("/show_full_jira_metrics", new ShowJiraMetricsCommand(bot, ShowJiraMetricsCommand.SHOW_JIRA_STATISTIC_FOR_ALL_PERIOD));
        rules.registerRule(commandExecutorRule);
        initCommonChecker(bot);
//        new JokesSender(bot).start();
        if (Common.JENKINS_ADDITIONAL_URL != null) {
            new JenkinsCheckerForAllStatuses(bot, TimeUnit.HOURS.toMillis(1), Common.JENKINS_ADDITIONAL_URL)
                .withIdleTimeoutMultiplier(5)
                .withMaxNumberOfAttempts(5)
                .start();
        }
        if (Common.JENKINS_URL != null) {
            new JenkinsChecker(bot, TimeUnit.MINUTES.toMillis(20), Common.JENKINS_URL)
                .withIdleTimeoutMultiplier(5)
                .withMaxNumberOfAttempts(5)
                .start();
        }
        for (Long chatId : Common.data.getMainGeneralChatIds()) {
            new EtsClarityChecker(bot, chatId, TimeUnit.MINUTES.toMillis(58), Common.ETS_DAY).start();
        }
        ConsoleLogger.additionalErrorLogger = message -> {
            BotHelper.logError(bot, message);
        };
//        initUpsourceCheckers(bot);
        bot.setUpdatesListener(updatess -> {
            if ("debug".equalsIgnoreCase(System.getProperty("debug"))) {
                System.out.println("onResponse: " + updatess.toString());
            }
            new Thread(() -> rules.handle(updatess)).start();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void initUpsourceCheckers(TelegramBot bot) {
        new UpsourceChecker(bot).start();
        LoginData loginData = getJiraLoginDataForChatsWithUpsource();
        if (loginData != null) {
            new UpsourceSendMailChecker(
                TimeUnit.MINUTES.toMillis(30),
                () -> JiraHelper.tryToGetClient(loginData,
                    true,
                    e -> ReLoginRule.tryToRelogin(bot, e, loginData)))
                .start();
        } else {
            String message = "no chats for initUpsourceCheckers";
            ConsoleLogger.logError(new RuntimeException(message), message);
        }
    }

    private static LoginData getJiraLoginDataForChatsWithUpsource() {
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            if (!generalChat.getUpsourceIds().isEmpty()) {
                return generalChat.getJiraConfig().getLoginData();
            }
        }

        return null;
    }

    private static void initCommonChecker(TelegramBot bot) {
        Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap = new HashMap<>();
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            if (generalChat.getJiraConfig() == null) {
                continue;
            }
            LoginData loginData = generalChat.getJiraConfig().getLoginData();
            jiraHelperServiceProviderMap.put(loginData.url, new JiraHelperServiceProvider(bot, loginData));
        }
        UpsourceServiceProvider upsourceServiceProvider = new UpsourceServiceProvider();
        new CommonCheckerWithRemoveOfPreviousMessages(bot, TimeUnit.HOURS.toMillis(2))
            .withChecker(new UnTrackedJiraIssuesWhichWasDoneChecker(jiraHelperServiceProviderMap))
            .withIdleTimeoutMultiplier(2)
            .withMaxNumberOfAttempts(5)
            .start();
        new CommonChecker(bot, TimeUnit.MINUTES.toMillis(10))
            .withChecker(new NewJiraIssuesChecker(jiraHelperServiceProviderMap))
            .withIdleTimeoutMultiplier(5)
            .withMaxNumberOfAttempts(5)
            .start();
        new CommonCheckerWithRemoveOfPreviousMessages(bot, TimeUnit.HOURS.toMillis(1))
            .withChecker(new UnEstimatedJiraIssuesChecker(jiraHelperServiceProviderMap))
            .withChecker(new UnTrackedJiraIssuesOnReviewChecker(jiraHelperServiceProviderMap, upsourceServiceProvider))
            .withIdleTimeoutMultiplier(2)
            .withMaxNumberOfAttempts(5)
            .start();
    }
}
