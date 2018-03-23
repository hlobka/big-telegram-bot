package telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.checker.JenkisChecker;
import telegram.bot.commands.*;
import telegram.bot.data.Common;
import telegram.bot.rules.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainBot {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        TelegramBot bot = new TelegramBot(Common.data.token);
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        System.out.println("onResponse: " + updates.toString());
        Rules rules = new Rules();
        rules.registerRule(new SlotMachineRule(bot));
        rules.registerRule(new AnswerRule(bot));
        rules.registerRule(new IIAnswerRule(bot));
        rules.registerRule(new EtsAnswerRule(bot));
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(bot);
        commandExecutorRule.addCommand("/get_chat_id", new GetChatIdCommand());
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
        rules.registerRule(commandExecutorRule);
        new JenkisChecker(bot, TimeUnit.MINUTES.toMillis(20), Common.JENKINS_URL).start();
        new EtsClarityChecker(bot, TimeUnit.MINUTES.toMillis(58)).start();
        bot.setUpdatesListener(updatess -> {
            System.out.println("onResponse: " + updatess.toString());
            rules.handle(updatess);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
