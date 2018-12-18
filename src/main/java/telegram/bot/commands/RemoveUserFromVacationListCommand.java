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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveUserFromVacationListCommand implements Command {

    private TelegramBot bot;

    public RemoveUserFromVacationListCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message replyToMessage = update.message().replyToMessage();
        if(replyToMessage != null) {
            Long chatId = replyToMessage.chat().id();
            User user = replyToMessage.from();
            returnUserFromVacation(user, chatId);
            return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s returns from vacation", user.firstName())));
        }
        HashMap<User, Boolean> users = Common.ETS_HELPER.getUsers();
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            User user = entry.getKey();
            int userId;
            try {
                userId = Integer.parseInt(values);
            } catch (NumberFormatException e){
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Invalid user id: %s", values)));
            }
            if(user.id() == userId){
                Long chatId = update.message().chat().id();
                returnUserFromVacation(user, chatId);
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s returns from vacation", user.firstName())));
            }
        }

        return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Unknown user with id: %s", values)));
    }

    private void returnUserFromVacation(User user, Long chatId) {
        if(Common.ETS_HELPER.isUserOnVacation(user)) {
            Common.ETS_HELPER.resolveUser(user, false);
        }
        EtsClarityChecker.updateLastMessage(bot, chatId);
    }
}
