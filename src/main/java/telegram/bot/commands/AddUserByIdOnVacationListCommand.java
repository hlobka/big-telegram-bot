package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;

import java.util.*;

public class AddUserByIdOnVacationListCommand implements Command {

    private TelegramBot bot;

    public AddUserByIdOnVacationListCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
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
                Common.ETS_HELPER.userOnVacation(user);
                EtsClarityChecker.updateLastMessage(bot, update.message().chat().id());
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s sent on vacation", user.firstName())));
            }
        }

        return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Unknown user with id: %s", values)));
    }
}
