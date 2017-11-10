package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import javafx.util.Pair;
import telegram.bot.commands.Command;
import telegram.bot.commands.ExecutableCommand;

import java.util.HashMap;
import java.util.Map;

import static helper.string.StringHelper.getRegString;

public class CommandExecutorRule implements Rule {
    private TelegramBot bot;
    private Map<String, Command> commands = new HashMap<>();

    public CommandExecutorRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        final String text = message.text() == null ? "" : message.text();
        final String command = getRegString(text, "(^\\/([a-zA-Z]+)((_[a-zA-Z]+)+)?)").toLowerCase();
        if (message.from().isBot() || command.isEmpty() || !command.startsWith("/")) {
            return;
        }
        String values = getRegString(text, "(\\/\\D\\w+)__(\\w+)@?.*", 2);
        Pair<ParseMode, String> result = getCommandResult(command, update, values);
        SendMessage request = new SendMessage(message.chat().id(), result.getValue())
            .parseMode(result.getKey())
            .disableWebPagePreview(false)
            .disableNotification(true)
            .replyToMessageId(message.messageId());
        bot.execute(request);
    }

    private Pair<ParseMode, String> getCommandResult(String command, Update update, String values) {
        if (!commands.containsKey(command.toLowerCase())) {
            return new Pair<>(ParseMode.Markdown, "Простите, данной комманды не сушествует.\n Попробуйте нажать /help");
        }
        return commands.get(command.toLowerCase()).run(update, values);
    }

    public void addCommand(String strCommand, Command command) {
        commands.put(strCommand.toLowerCase(), command);
    }
}
