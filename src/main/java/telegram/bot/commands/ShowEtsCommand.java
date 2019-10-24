package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;

import java.util.Collections;
import java.util.List;

public class ShowEtsCommand implements Command {
    private final TelegramBot bot;

    public ShowEtsCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        Integer userId = message.from().id();
        boolean hasChatData = Common.data.hasChatData(chatId);
        boolean userHasAccess = Common.data.telegramUserIdsWithGeneralAccess.contains(userId);
        if (hasChatData || userHasAccess) {
            EtsClarityChecker.sendNotification(chatId, bot);
            return new Pair<>(ParseMode.HTML, Collections.singletonList(""));
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Sorry, you cannot have access for this command"));
    }
}
