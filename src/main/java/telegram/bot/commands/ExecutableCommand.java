package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
public interface ExecutableCommand extends Command {
    Integer getNumOfArguments();
}
