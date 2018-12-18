package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;

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
        EtsClarityChecker.sendNotification(message.chat().id(), bot);
        return new Pair<>(ParseMode.HTML, Collections.singletonList(""));
    }
}
