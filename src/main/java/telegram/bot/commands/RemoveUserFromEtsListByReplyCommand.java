package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static telegram.bot.data.Common.ETS_USERS;
import static telegram.bot.data.Common.ETS_USERS_IN_VACATION;

public class RemoveUserFromEtsListByReplyCommand implements Command {

    private TelegramBot bot;

    public RemoveUserFromEtsListByReplyCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<>());
        ArrayList<User> usersInVacation = SharedObject.loadList(ETS_USERS_IN_VACATION, new ArrayList<>());

        Message replyToMessage = update.message().replyToMessage();
        if(replyToMessage != null) {
            User replyUser = replyToMessage.from();
            if(!users.containsKey(replyUser)){
                users.remove(replyUser, false);
            }
            if(!usersInVacation.contains(replyUser)) {
                usersInVacation.remove(replyUser);
            }
            SharedObject.save(ETS_USERS_IN_VACATION, usersInVacation);
            SharedObject.save(ETS_USERS, users);
            EtsClarityChecker.updateLastMessage(bot);
            return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s was removed from ETS list", replyUser.firstName())));
        }
        return new Pair<>(ParseMode.Markdown, Collections.singletonList("Please, use this command with reply from who would leave from ETS list"));
    }
}
