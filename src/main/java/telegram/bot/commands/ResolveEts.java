package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.EtsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolveEts implements Command {
    private TelegramBot bot;

    public ResolveEts(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        ArrayList<String> strings = new ArrayList<>();
        User user = message.from();
        strings.add("Ets resolved for: " + user.firstName());
        resolveUser(user, bot, message.chat().id());
        return new Pair<>(ParseMode.HTML, strings);
    }

    public static void sendUserOnVocation(User user, TelegramBot bot, long chatId) {
        if(!Common.ETS_HELPER.isUserOnVacation(user)) {
            Common.ETS_HELPER.userOnVacation(user);
            String message = String.format("user %s sent on vacation", user.firstName());
            for (Long mainGeneralChatId : Common.data.getMainGeneralChatIds()) {
                BotHelper.sendMessage(bot, mainGeneralChatId, message, ParseMode.Markdown);
            }
        }
    }

    public static void resolveUser(User user, TelegramBot bot, long chatId) {
        Common.ETS_HELPER.resolveUser(user);
        EtsClarityChecker.updateLastMessage(bot, chatId);
        if (EtsClarityChecker.checkIsResolvedToDay(bot, chatId)) {
            for (Long mainGeneralChatId : Common.data.getMainGeneralChatIds()) {
                BotHelper.sendMessage(bot, mainGeneralChatId, "EtsClarity resolved today!!!", ParseMode.Markdown);
            }
        }
    }

    public static void setUserAsWithIssue(User user, TelegramBot bot, Long chatId) {
        Common.ETS_HELPER.userHasIssue(user);
        EtsClarityChecker.updateLastMessage(bot, chatId);
    }

}
