package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.helper.EtsHelper;

import java.util.*;

import static telegram.bot.data.Common.ETS_USERS;
import static telegram.bot.data.Common.ETS_USERS_IN_VACATION;

public class SendUserOnVacationByReplyListCommand implements Command {

    private TelegramBot bot;

    public SendUserOnVacationByReplyListCommand(TelegramBot bot) {
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
                users.put(replyUser, false);
            }
            if(!usersInVacation.contains(replyUser)) {
                usersInVacation.add(replyUser);
            }
            EtsHelper.clearFromDuplicates(users);
            SharedObject.save(ETS_USERS_IN_VACATION, usersInVacation);
            SharedObject.save(ETS_USERS, users);
            EtsClarityChecker.updateLastMessage(bot);
            return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user %s sent on vacation", replyUser.firstName())));
        }
        return new Pair<>(ParseMode.Markdown, Collections.singletonList("Please, use this command with reply from who would go to vacation"));
    }
}
