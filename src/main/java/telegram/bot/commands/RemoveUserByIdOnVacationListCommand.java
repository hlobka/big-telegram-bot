package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;

import java.util.*;

import static telegram.bot.data.Common.ETS_USERS;
import static telegram.bot.data.Common.ETS_USERS_IN_VACATION;

public class RemoveUserByIdOnVacationListCommand implements Command {

    private TelegramBot bot;

    public RemoveUserByIdOnVacationListCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {

        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            User user = entry.getKey();
            int userId;
            try {
                userId = Integer.parseInt(values);
            } catch (NumberFormatException e){
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Invalid user id: %s", values)));
            }
            if(user.id() == userId){
                ArrayList<User> usersInVacation = SharedObject.loadList(ETS_USERS_IN_VACATION, new ArrayList<User>());
                if(usersInVacation.contains(user)){
                    usersInVacation.remove(user);
                    SharedObject.save(ETS_USERS_IN_VACATION, usersInVacation);
                }
                EtsClarityChecker.updateLastMessage(bot);
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s returns from vacation", user.firstName())));
            }
        }

        return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Unknown user with id: %s", values)));
    }
}
