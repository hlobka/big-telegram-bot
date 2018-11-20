package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.EtsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        resolveUser(user, bot, message.chat().id());
        return new Pair<>(ParseMode.HTML, strings);
    }

    private static void returnUserFromVocation(User user, TelegramBot bot, long chatId) {
        updateUserInVocationList(user, bot, false, chatId);
    }

    public static void sendUserOnVocation(User user, TelegramBot bot, long chatId) {
        updateUserInVocationList(user, bot, true, chatId);
    }

    private static void updateUserInVocationList(User user, TelegramBot bot, boolean addUserInVocationList, long chatId) {
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            User entryUser = entry.getKey();
            if (entryUser.id().equals(user.id())) {
                ArrayList<User> usersInVacation = EtsHelper.getUsersFromVacation();
                if (addUserInVocationList) {
                    if (!usersInVacation.contains(user)) {
                        usersInVacation.add(user);
                        EtsHelper.saveUsersWhichInVacation(usersInVacation);
                    }
                    EtsClarityChecker.updateLastMessage(bot, chatId);
                    String message = String.format("user %s sent on vacation", user.firstName());
                    for (Long mainGeneralChatId : Common.data.getMainGeneralChatIds()) {
                        BotHelper.sendMessage(bot, mainGeneralChatId, message, ParseMode.Markdown);
                    }

                } else {
                    Boolean isContains = false;
                    while (usersInVacation.contains(user)) {
                        usersInVacation.remove(user);
                        isContains = true;
                    }
                    if (isContains) {
                        EtsHelper.saveUsersWhichInVacation(usersInVacation);
                        EtsClarityChecker.updateLastMessage(bot, chatId);
                        String message = String.format("user %s returns from vacation", user.firstName());
                        for (Long mainGeneralChatId : Common.data.getMainGeneralChatIds()) {
                            BotHelper.sendMessage(bot, mainGeneralChatId, message, ParseMode.Markdown);
                        }

                    }
                }

            }
        }
    }

    public static void resolveUser(User user, TelegramBot bot, long chatId) {
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        ArrayList<User> usersToRemove = new ArrayList<>();
        for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
            if(userBooleanEntry.getKey().id().equals(user.id())){
                usersToRemove.add(userBooleanEntry.getKey());
            }
        }
        for (User userToRemove : usersToRemove) {
            users.remove(userToRemove);
        }
        users.put(user, true);
        EtsHelper.clearFromDuplicates(users);
        returnUserFromVocation(user, bot, chatId);
        EtsHelper.saveUsers(users);
        EtsClarityChecker.updateLastMessage(bot, chatId);
        if (EtsClarityChecker.checkIsResolvedToDay(bot, chatId)) {
            for (Long mainGeneralChatId : Common.data.getMainGeneralChatIds()) {
                BotHelper.sendMessage(bot, mainGeneralChatId, "EtsClarity resolved today!!!", ParseMode.Markdown);
            }
        }
    }
}
