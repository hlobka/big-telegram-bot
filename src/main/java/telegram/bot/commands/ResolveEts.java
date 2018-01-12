package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;

import java.util.*;

import static telegram.bot.data.Common.ETS_USERS;

public class ResolveEts implements Command {
    private TelegramBot bot;

    public ResolveEts(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        ArrayList<String> strings = new ArrayList<>();
        User user = message.from();
        strings.add("Ets resolved for: " + user.firstName());
        resolveUser(user, bot);
        return new Pair<>(ParseMode.HTML, strings);
    }

    public static void resolveUser(User user, TelegramBot bot) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        users.put(user, true);
        clearFromDuplicates(users);
        SharedObject.save(ETS_USERS, users);
        EtsClarityChecker.updateLastMessage(bot);
    }

    private static void clearFromDuplicates(HashMap<User, Boolean> users) {
        List<User> userList = new ArrayList<>();
        Set<Map.Entry<User, Boolean>> entries = users.entrySet();
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User user = userBooleanEntry.getKey();
            if(isUserPresent(user, entries)){
                userList.add(user);
            }
        }
        for (User user : userList) {
            users.remove(user);
        }
    }

    private static boolean isUserPresent(User user, Set<Map.Entry<User, Boolean>> entries) {
        Integer amount = 0;
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User entryKey = userBooleanEntry.getKey();
            if(!entryKey.equals(user) && Objects.equals(entryKey.id(), user.id())){
                amount++;
            }
        }
        return amount > 0;
    }
}
