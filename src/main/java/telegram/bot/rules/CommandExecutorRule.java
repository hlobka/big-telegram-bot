package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import javafx.util.Pair;
import telegram.bot.commands.Command;
import telegram.bot.data.TelegramCriteria;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static helper.string.StringHelper.getRegString;

public class CommandExecutorRule implements Rule {
    private TelegramBot bot;
    private Map<String, Command> commands = new HashMap<>();
    private Map<String, Command> callBackCommands = new HashMap<>();

    public CommandExecutorRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void callback(Update update) {
        String data = update.callbackQuery().data();
        if(callBackCommands.containsKey(data)){
            callBackCommands.get(data).run(update, data);
        }
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
        Pair<ParseMode, List<String>> result = getCommandResult(command, update, values);
        for (String messageStr : result.getValue()) {
            sendMessage(message, new Pair<>(result.getKey(), messageStr));
        }
    }

    private void sendMessage(Message message, Pair<ParseMode, String> result) {
        String value = result.getValue();
        int length = value.length();
        if (length >= TelegramCriteria.MAX_MESSAGE_LENGTH) {
            value = String.format("Превышина максимальная длина сообщения. \n Текущая %d из допустимых %d", length, TelegramCriteria.MAX_MESSAGE_LENGTH);
        }
        if (result.getKey() == ParseMode.HTML) {
            value = value.replaceAll("\\[", "")
                .replaceAll("]", "");
        }
        SendMessage request = new SendMessage(message.chat().id(), value)
            .parseMode(result.getKey())
            .disableWebPagePreview(false)
            .disableNotification(true)
            .replyToMessageId(message.messageId());
        bot.execute(request);
    }

    private Pair<ParseMode, List<String>> getCommandResult(String command, Update update, String values) {
        if (!commands.containsKey(command.toLowerCase())) {
            return new Pair<>(ParseMode.Markdown, Collections.singletonList("Простите, данной комманды не сушествует.\n Попробуйте нажать /help"));
        }
        return commands.get(command.toLowerCase()).run(update, values);
    }

    public void addCommand(String strCommand, Command command) {
        commands.put(strCommand.toLowerCase(), command);
    }

    public void addCallBackCommand(String strCommand, Command command) {
        callBackCommands.put(strCommand, command);
    }
}
