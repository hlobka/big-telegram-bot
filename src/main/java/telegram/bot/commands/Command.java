package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;

@FunctionalInterface
public interface Command {
    Pair<ParseMode, String> run(Update update, String values);
}
