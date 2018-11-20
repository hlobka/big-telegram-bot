package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.GetChatMembersCount;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatMembersCountResponse;
import com.pengrad.telegrambot.response.SendResponse;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import telegram.bot.helper.EtsHelper;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.log;
import static telegram.bot.data.Common.COMMON_INT_DATA;
//todo: remake without static fields
public class EtsClarityChecker extends Thread {
    private TelegramBot bot;
    private final long chatId;
    private long millis;
    private static boolean isResolvedToday = false;
    private static Integer LAST_MESSAGE_ID = -1;
    private static long LAST_MESSAGE_CHAT_ID = -1;
    private static DayOfWeek DAY_TO_CHECK;

    public EtsClarityChecker(TelegramBot bot, long chatId, long millis, DayOfWeek dayToCheck) {
        this.bot = bot;
        this.chatId = chatId;
        this.millis = millis;
        EtsClarityChecker.DAY_TO_CHECK = dayToCheck;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        LAST_MESSAGE_CHAT_ID = commonData.getOrDefault("LAST_MESSAGE_CHAT_ID", chatId).longValue();
        LAST_MESSAGE_ID = commonData.getOrDefault("LAST_MESSAGE_ID", 2472).intValue();
    }

    @Override
    public void run() {
        log("EtsClarityChecker::start");
        super.run();
        while (true) {
            long timeout = TimeUnit.MINUTES.toMillis(getTimeout());
            long oneMinuteInMilliseconds = TimeUnit.MINUTES.toMillis(1);
            long min = Math.max(oneMinuteInMilliseconds, Math.min(millis, timeout));
            if (min > 0) {
                sleep(min, TimeUnit.MILLISECONDS);
            }
            check();
        }
    }

    private void check() {
        System.out.println("EtsClarityChecker::check");
        if (TimeHelper.checkToDayIs(DAY_TO_CHECK)) {
            checkIsAllUsersPresentsOnThisChat(bot, chatId);
            if (checkIsResolvedToDay(bot, chatId)) {
                if(!isResolvedToday){
                    isResolvedToday = true;

                }
                return;
            }
            Calendar calendar = Calendar.getInstance();
            int currentTimeInHours = calendar.get(Calendar.HOUR_OF_DAY);
            int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
            if (currentTimeInHours >= 10 && currentTimeInHours < 23) {
                int timeout = getTimeout();
                if (timeout > 0) {
                    sleep(timeout, TimeUnit.MINUTES);
                }
                sendNotification(chatId);
                System.out.println(new Date().getTime() + "::EtsClarityChecker::TRUE; Hours: " + currentTimeInHours + "; Minutes: " + currentTimeInMinutes);
            }
        } else {
            unResolveAll();
        }
    }

    private int getTimeout() {
        Calendar calendar = Calendar.getInstance();
        int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
        return 59 - currentTimeInMinutes;
    }

    private void sendNotification(long chatId) {
        String message = getMessage(bot, chatId);
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Resolve").callbackData("ets_resolved"),
                new InlineKeyboardButton("On Vacation").callbackData("ets_on_vacation"),
            }));
        SendResponse execute = bot.execute(request);
        LAST_MESSAGE_ID = execute.message().messageId();
        LAST_MESSAGE_CHAT_ID = chatId;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        commonData.put("LAST_MESSAGE_ID", LAST_MESSAGE_ID);
        commonData.put("LAST_MESSAGE_CHAT_ID", LAST_MESSAGE_CHAT_ID);
        SharedObject.save(COMMON_INT_DATA, commonData);
    }

    public static void updateLastMessage(TelegramBot bot, long chatId) {
        if (LAST_MESSAGE_ID == -1 || LAST_MESSAGE_CHAT_ID == -1) {
            System.out.println(String.format("WARN::Couldn't updateLastMessage with CHAT_ID: %d, and MESSAGE_ID: %d", LAST_MESSAGE_CHAT_ID, LAST_MESSAGE_ID));
            return;
        }
        if(!TimeHelper.checkToDayIs(DAY_TO_CHECK)){
            unResolveAll();
        }
        try {
            EditMessageText request = new EditMessageText(LAST_MESSAGE_CHAT_ID, LAST_MESSAGE_ID, getMessage(bot, chatId))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                    new InlineKeyboardButton("Resolve").callbackData("ets_resolved"),
                    new InlineKeyboardButton("On Vacation").callbackData("ets_on_vacation"),
                }));
            bot.execute(request);
        } catch (RuntimeException e) {
            ConsoleLogger.logErrorFor(EtsClarityChecker.class, e);
        }
    }

    private static void unResolveAll() {
        isResolvedToday = false;
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
            userBooleanEntry.setValue(false);
        }
        EtsHelper.saveUsers(users);
        LAST_MESSAGE_ID = -1;
        LAST_MESSAGE_CHAT_ID = -1;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        commonData.put("LAST_MESSAGE_ID", LAST_MESSAGE_ID);
        commonData.put("LAST_MESSAGE_CHAT_ID", LAST_MESSAGE_CHAT_ID);
        SharedObject.save(COMMON_INT_DATA, commonData);
    }

    public static String getMessage(TelegramBot bot, long chatId) {
        String message = getMessageFromFile();
        String users = getUsers(bot, chatId);
        message = message + users;
        return message;
    }

    private static String getUsers(TelegramBot bot, long chatId) {
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(chatId));
        int count = response.count();
        StringBuilder resolvedUsers = new StringBuilder();
        int resolvedCount = 0;
        ArrayList<User> usersInVacation = EtsHelper.getUsersFromVacation();

        if (!users.isEmpty()) {
            for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
                User user = userBooleanEntry.getKey();
                Boolean resolved = userBooleanEntry.getValue();
                if (!user.isBot()) {
                    if(usersInVacation.contains(user)){
                        resolved = true;
                        resolvedUsers.append(String.format("%s %s : %s%n", user.firstName(), user.lastName(), "🍌"));
                    } else {
                        resolvedUsers.append(String.format("%s %s : %s%n", user.firstName(), user.lastName(), resolved ? "🍏" : "🍎"));
                    }
                }
                if (resolved) {
                    resolvedCount++;
                }
            }
        }
        int expectedCount = count - 1 - usersInVacation.size();
        isResolvedToday = resolvedCount >= expectedCount;
        return resolvedUsers.toString() + String.format("%nResolved: %d/%d%n", resolvedCount, count - 1);
    }

    public static void checkIsAllUsersPresentsOnThisChat(TelegramBot bot, long chatId) {
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        ArrayList<User> usersInVacation = EtsHelper.getUsersFromVacation();
        List<Integer> allUsersId = new ArrayList<>();

        List<User> usersToRemove = new ArrayList<>();
        List<User> usersToRemoveFromVacation = new ArrayList<>();
        for (User user : users.keySet()) {
            if(!allUsersId.contains(user.id())) {
                allUsersId.add(user.id());
            }
        }
        for (User user : usersInVacation) {
            if(!allUsersId.contains(user.id())) {
                allUsersId.add(user.id());
            }
        }
        for (Integer userId : allUsersId) {
            GetChatMemberResponse response = bot.execute(new GetChatMember(chatId, userId));
            ChatMember chatMember = response.chatMember();
            if(chatMember ==null || chatMember.status().equals(ChatMember.Status.left)) {
                for (User user : users.keySet()) {
                    if(user.id().equals(userId)) {
                        usersToRemove.add(user);
                    }
                }
                for (User user : usersInVacation) {
                    if(user.id().equals(userId)) {
                        usersToRemoveFromVacation.add(user);
                    }
                }
            }
        }
        for (User userFromVacation : usersInVacation) {
            for (User user : users.keySet()) {
                if(user.id().equals(userFromVacation.id()) && !user.equals(userFromVacation)){
                    usersToRemoveFromVacation.add(userFromVacation);
                }
            }
        }
        for (User user : usersToRemove) {
            users.remove(user);
        }
        for (User user : usersToRemoveFromVacation) {
            usersInVacation.remove(user);
        }

        if(!usersToRemoveFromVacation.isEmpty()) {
            EtsHelper.saveUsersWhichInVacation(usersInVacation);
        }
        if(!usersToRemove.isEmpty()) {
            EtsHelper.saveUsers(users);
        }
    }

    public static Boolean checkIsResolvedToDay(TelegramBot bot, long chatId) {
        int resolvedCount = 0;
        HashMap<User, Boolean> users = EtsHelper.getUsers();
        ArrayList<User> usersInVacation = EtsHelper.getUsersFromVacation();
        if (!users.isEmpty()) {
            for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
                User user = userBooleanEntry.getKey();
                Boolean resolved = userBooleanEntry.getValue();
                if (user.isBot()) {
                    continue;
                }
                if (resolved) {
                    resolvedCount++;
                }
            }
        }
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(chatId));
        int count = response.count();
        int expectedCount = count - 1 - usersInVacation.size();
        return TimeHelper.checkToDayIs(DAY_TO_CHECK) && resolvedCount >= expectedCount;
    }

    private static String getMessageFromFile() {
        String message = null;
        try {
            message = StringHelper.getFileAsString("ets_clarity.html");
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(EtsClarityChecker.class, e);
        }
        return message;
    }

    private void sleep(long timeout, TimeUnit timeUnit) {
        log("EtsClarityChecker::will be wait: " + timeout + " " + timeUnit.name());
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            ConsoleLogger.logErrorFor(this, e);
            Thread.currentThread().interrupt();
        }
    }
}
