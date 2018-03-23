package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;

public class GetChatIdCommand implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("chat id is: %d", chatId)));
    }
}
