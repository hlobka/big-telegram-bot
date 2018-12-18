package telegram.bot;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import helper.logger.ConsoleLogger;
import okhttp3.OkHttpClient;
import telegram.bot.checker.*;
import telegram.bot.commands.*;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.*;
import telegram.bot.rules.like.LikeAnswerRule;

import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainBot {
    public static void main(String[] args) throws URISyntaxException {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        TelegramBot bot = TelegramBotAdapter.buildCustom(Common.data.token,client);
//        TelegramBot bot = new TelegramBot(Common.data.token);
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        System.out.println("onResponse: " + updates.toString());
        Rules rules = new Rules();
        /*rules.registerRule(new SlotMachineRule(bot));
        rules.registerRule(new AnswerRule(bot));
        rules.registerRule(new IIAnswerRule(bot));
        rules.registerRule(new EtsAnswerRule(bot));
        rules.registerRule(new LikeAnswerRule(bot));
        rules.registerRule(new BotSayAnswerRule(bot));
        rules.registerRule(new ReLoginRule(bot));*/
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(bot);
        commandExecutorRule.addCallBackCommand("update_upsource_checker_view_result_for", new UpdateUpsourceViewResult(bot));
        commandExecutorRule.addCallBackCommand("show_upsource_checker_tabs_description", new ShowAlertFromResource(Common.UPSOURCE.checkerHelpLink, bot));
        commandExecutorRule.addCommand("/get_chat_id", new GetChatIdCommand());
        commandExecutorRule.addCommand("/get_user_id_by_name", new GetUserIdByNameCommand());
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
        rules.registerRule(commandExecutorRule);
//        new JokesSender(bot).start();
        new JiraChecker(bot, TimeUnit.MINUTES.toMillis(20)).start();
        new JenkinsChecker(bot, TimeUnit.MINUTES.toMillis(20), Common.JENKINS_URL).start();
        for (Long chatId : Common.data.getMainGeneralChatIds()) {
            //todo: move day to config file
            new EtsClarityChecker(bot, chatId, TimeUnit.MINUTES.toMillis(58), DayOfWeek.TUESDAY).start();
        }

        new UpsourceChecker(bot).start();
        ConsoleLogger.additionalErrorLogger = message -> {
            BotHelper.logError(bot, message);
        };
        new UpsourceSendMailChecker(TimeUnit.MINUTES.toMillis(30), () -> JiraHelper.tryToGetClient(Common.JIRA, true, e -> {
            ReLoginRule.tryToRelogin(bot, e);
        })).start();
        bot.setUpdatesListener(updatess -> {
            if ("debug".equalsIgnoreCase(System.getProperty("debug"))) {
                System.out.println("onResponse: " + updatess.toString());
            }
            new Thread(() -> rules.handle(updatess)).start();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
