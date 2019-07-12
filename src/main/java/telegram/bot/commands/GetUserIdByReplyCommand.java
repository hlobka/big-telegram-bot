package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;

public class GetUserIdByReplyCommand implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message replyToMessage = update.message().replyToMessage();
        if (replyToMessage != null) {
            User replyUser = replyToMessage.forwardFrom() != null ? replyToMessage.forwardFrom() : replyToMessage.from();
            return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user id is : %d", replyUser.id())));
        }

        return new Pair<>(ParseMode.Markdown, Collections.singletonList("Try to use reply of user to get his id"));
    }
}
