package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;

import java.util.*;

import static telegram.bot.data.Common.ETS_USERS;
import static telegram.bot.data.Common.ETS_USERS_IN_VACATION;

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

    public static void returnUserFromVocation(User user, TelegramBot bot) {
        updateUserInVocationList(user, bot, false);
    }

    public static void sendUserOnVocation(User user, TelegramBot bot) {
        updateUserInVocationList(user, bot, true);
    }

    private static void updateUserInVocationList(User user, TelegramBot bot, boolean addUserInVocationList) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            User entryUser = entry.getKey();
            if (entryUser.id().equals(user.id())) {
                ArrayList<User> usersInVacation = SharedObject.loadList(ETS_USERS_IN_VACATION, new ArrayList<User>());
                if (addUserInVocationList) {
                    if (!usersInVacation.contains(user)) {
                        usersInVacation.add(user);
                        SharedObject.save(ETS_USERS_IN_VACATION, usersInVacation);
                    }
                    EtsClarityChecker.updateLastMessage(bot);
                    String message = String.format("user %s sent on vacation", user.firstName());
                    BotHelper.sendMessage(bot, Common.BIG_GENERAL_CHAT_ID, message, ParseMode.Markdown);
                } else {
                    Boolean isContains = false;
                    while (usersInVacation.contains(user)) {
                        usersInVacation.remove(user);
                        isContains = true;
                    }
                    if (isContains) {
                        SharedObject.save(ETS_USERS_IN_VACATION, usersInVacation);
                        EtsClarityChecker.updateLastMessage(bot);
                        String message = String.format("user %s returns from vacation", user.firstName());
                        BotHelper.sendMessage(bot, Common.BIG_GENERAL_CHAT_ID, message, ParseMode.Markdown);
                    }
                }

            }
        }
    }

    public static void resolveUser(User user, TelegramBot bot) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        users.put(user, true);
        clearFromDuplicates(users);
        returnUserFromVocation(user, bot);
        SharedObject.save(ETS_USERS, users);
        EtsClarityChecker.updateLastMessage(bot);
        if (EtsClarityChecker.checkIsResolvedToDay(bot)) {
            BotHelper.sendMessage(bot, Common.BIG_GENERAL_CHAT_ID, "EtsClarity resolved today!!!", ParseMode.Markdown);
        }
    }

    private static void clearFromDuplicates(HashMap<User, Boolean> users) {
        List<User> userList = new ArrayList<>();
        Set<Map.Entry<User, Boolean>> entries = users.entrySet();
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User user = userBooleanEntry.getKey();
            if (isUserPresent(user, entries)) {
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
            if (!entryKey.equals(user) && Objects.equals(entryKey.id(), user.id())) {
                amount++;
            }
        }
        return amount > 0;
    }
}
