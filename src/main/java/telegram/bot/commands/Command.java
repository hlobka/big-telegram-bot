package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;

import java.util.List;

@FunctionalInterface
public interface Command {
    Pair<ParseMode, List<String>> run(Update update, String values);
}
