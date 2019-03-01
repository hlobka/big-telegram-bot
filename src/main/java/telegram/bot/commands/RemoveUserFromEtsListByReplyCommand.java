package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import java.util.Collections;
import java.util.List;

public class RemoveUserFromEtsListByReplyCommand implements Command {

    private TelegramBot bot;

    public RemoveUserFromEtsListByReplyCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message replyToMessage = update.message().replyToMessage();
        if(replyToMessage != null) {
            User replyUser = replyToMessage.from();
            Common.ETS_HELPER.removeUser(replyUser);
            EtsClarityChecker.updateLastMessage(bot, update.message().chat().id());
            return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s was removed from ETS list", replyUser.firstName())));
        }
        return new Pair<>(ParseMode.Markdown, Collections.singletonList("Please, use this command with reply from who would leave from ETS list"));
    }
}
