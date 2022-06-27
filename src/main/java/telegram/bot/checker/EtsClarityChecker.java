package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatMembersCountResponse;
import com.pengrad.telegrambot.response.SendResponse;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.EtsHelper;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.logFor;
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
        logFor(this, "start");
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
        System.out.println("EtsChecker::check");
        if (TimeHelper.checkToDayIs(DAY_TO_CHECK)) {
            checkIsAllUsersPresentsOnThisChat(bot, chatId);
            if (checkIsResolvedToDay(bot, chatId)) {
                if (!isResolvedToday) {
                    notifyThatIsResolvedToDay();
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
                if (!isResolvedToday) {
                    sendNotification(chatId);
                    System.out.println(new Date().getTime() + "::EtsClarityChecker::TRUE; Hours: " + currentTimeInHours + "; Minutes: " + currentTimeInMinutes);
                }
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
        sendNotification(chatId, bot);
    }

    public static void sendNotification(long chatId, TelegramBot bot) {
        String message = getMessage(bot, chatId);
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Resolve").callbackData("ets_resolved"),
                new InlineKeyboardButton("On Vacation").callbackData("ets_on_vacation"),
                new InlineKeyboardButton("Has Issues").callbackData("ets_with_issue"),
                new InlineKeyboardButton("Approve Issues").callbackData("ets_approve_users_with_issues"),
            }));
        removeLastNotification(bot, chatId);
        SendResponse execute = bot.execute(request);
        LAST_MESSAGE_ID = execute.message().messageId();
        LAST_MESSAGE_CHAT_ID = chatId;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        commonData.put("LAST_MESSAGE_ID", LAST_MESSAGE_ID);
        commonData.put("LAST_MESSAGE_CHAT_ID", LAST_MESSAGE_CHAT_ID);
        SharedObject.save(COMMON_INT_DATA, commonData);
        bot.execute(new PinChatMessage(chatId, LAST_MESSAGE_ID));
    }

    private static void removeLastNotification(TelegramBot bot, long chatId) {
        if (LAST_MESSAGE_CHAT_ID == chatId && LAST_MESSAGE_ID != -1) {
            BotHelper.removeMessage(bot, chatId, LAST_MESSAGE_ID);
        }
    }

    public static void updateLastMessage(TelegramBot bot, long chatId) {
        if (LAST_MESSAGE_ID == -1 || LAST_MESSAGE_CHAT_ID == -1) {
            System.out.println(String.format("WARN::Couldn't updateLastMessage with CHAT_ID: %d, and MESSAGE_ID: %d", LAST_MESSAGE_CHAT_ID, LAST_MESSAGE_ID));
            return;
        }
        if (!TimeHelper.checkToDayIs(DAY_TO_CHECK)) {
            unResolveAll();
        }
        try {
            EditMessageText request = new EditMessageText(LAST_MESSAGE_CHAT_ID, LAST_MESSAGE_ID, getMessage(bot, chatId))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                    new InlineKeyboardButton("Resolve").callbackData("ets_resolved"),
                    new InlineKeyboardButton("On Vacation").callbackData("ets_on_vacation"),
                    new InlineKeyboardButton("Has Issues").callbackData("ets_with_issue"),
                    new InlineKeyboardButton("Approve Issues").callbackData("ets_approve_users_with_issues"),
                }));
            bot.execute(request);
        } catch (RuntimeException e) {
            ConsoleLogger.logErrorFor(EtsClarityChecker.class, e);
        }
    }

    private static void unResolveAll() {
        isResolvedToday = false;
        Common.ETS_HELPER.unResolveAllUsualUsers();
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
        EtsHelper etsHelper = Common.ETS_HELPER;
        HashMap<User, Boolean> users = etsHelper.getUsers();
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(chatId));
        int count = response.count();
        StringBuilder resolvedUsers = new StringBuilder();
        int resolvedCount = 0;
        ArrayList<User> usersInVacation = etsHelper.getUsersFromVacation();

        if (!users.isEmpty()) {
            for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
                User user = userBooleanEntry.getKey();
                Boolean resolved = userBooleanEntry.getValue();
                if (!user.isBot()) {
                    if (etsHelper.isUserHasApprovedIssue(user)) {
                        resolvedUsers.append(String.format("%s %s : %s%n", user.firstName(), user.lastName(), "💊"));
                    } else if (etsHelper.isUserHasIssue(user)) {
                        resolvedUsers.append(String.format("%s %s : %s%n", user.firstName(), user.lastName(), "🦠"));
                    } else if (etsHelper.isUserOnVacation(user)) {
                        resolvedUsers.append(String.format("%s %s : %s%n", user.firstName(), user.lastName(), "🍹"));
                    } else {
                        String userName = getUserName(user, resolved);
                        resolvedUsers.append(String.format("%s : %s%n", userName, resolved ? "🍏" : "🍎"));
                    }
                }
                if (resolved) {
                    resolvedCount++;
                }
            }
        }
        isResolvedToday = isResolvedToday(resolvedCount, count);
        return resolvedUsers.toString() + String.format("%nResolved: %d/%d%n", resolvedCount, count - 1);
    }

    private static Boolean isResolvedToday(int resolvedCount, int count) {
        Boolean result;
        EtsHelper etsHelper = Common.ETS_HELPER;
        ArrayList<User> usersInVacation = etsHelper.getUsersFromVacation();
        ArrayList<User> usersWithApprovedIssues = etsHelper.getUsersWhichHaveApprovedIssues();

        int expectedCount = count - 1 - usersInVacation.size() - usersWithApprovedIssues.size();
        result = resolvedCount >= expectedCount;
        return result;
    }

    private static String getUserName(User user, Boolean resolved) {
        String userName;
        if (resolved) {
            userName = user.firstName() + " " + user.lastName();
        } else {
            userName = BotHelper.getLinkOnUser(user, ParseMode.HTML);
        }
        return userName;
    }

    public static void checkIsAllUsersPresentsOnThisChat(TelegramBot bot, long chatId) {
        HashMap<User, Boolean> users = Common.ETS_HELPER.getUsers();
        for (User user : users.keySet()) {
            if (!isUserChatMember(user, chatId, bot)) {
                Common.ETS_HELPER.removeUser(user);
            }
        }
    }

    private static boolean isUserChatMember(User user, long chatId, TelegramBot bot) {
        GetChatMemberResponse response = bot.execute(new GetChatMember(chatId, user.id()));
        ChatMember chatMember = response.chatMember();
        return chatMember != null && !chatMember.status().equals(ChatMember.Status.left);
    }

    public static Boolean checkIsResolvedToDay(TelegramBot bot, long chatId) {
        int resolvedCount = 0;
        int botsCount = 1;
        EtsHelper etsHelper = Common.ETS_HELPER;
        HashMap<User, Boolean> users = etsHelper.getUsers();
        for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
            User user = userBooleanEntry.getKey();
            Boolean resolved = userBooleanEntry.getValue();
            if ((resolved || etsHelper.isUserOnVacation(user)) || etsHelper.isUserHasApprovedIssue(user)) {
                resolvedCount++;
            }
        }
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(chatId));
        int count = response.count() - botsCount;
        return TimeHelper.checkToDayIs(DAY_TO_CHECK) && resolvedCount >= count;
    }

    public void notifyThatIsResolvedToDay() {
        BotHelper.sendMessage(bot, chatId, "Ets resolved today!!!", ParseMode.Markdown);
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
        logFor(this, "will be wait: " + timeout + " " + timeUnit.name());
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            ConsoleLogger.logErrorFor(this, e);
            Thread.currentThread().interrupt();
        }
    }
}
