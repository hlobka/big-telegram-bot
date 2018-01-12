package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetChatMembersCount;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetChatMembersCountResponse;
import com.pengrad.telegrambot.response.SendResponse;
import helper.file.SharedObject;
import helper.string.StringHelper;
import telegram.bot.data.Common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static telegram.bot.data.Common.COMMON_INT_DATA;
import static telegram.bot.data.Common.ETS_USERS;

public class EtsClarityChecker extends Thread {
    private TelegramBot bot;
    private long millis;
    private static boolean isResolvedToday = false;
    public static Integer LAST_MESSAGE_ID = -1;
    public static long LAST_MESSAGE_CHAT_ID = -1;

    public EtsClarityChecker(TelegramBot bot, long millis) throws URISyntaxException {
        this.bot = bot;
        this.millis = millis;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        LAST_MESSAGE_CHAT_ID = commonData.getOrDefault("LAST_MESSAGE_CHAT_ID", 2472).longValue();
        LAST_MESSAGE_ID = commonData.getOrDefault("LAST_MESSAGE_ID", Common.BIG_GENERAL_CHAT_ID).intValue();
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                long timeout = TimeUnit.MINUTES.toMillis(getTimeout());
                long oneMinuteInMilliseconds = TimeUnit.MINUTES.toMillis(1);
                long min = Math.max(oneMinuteInMilliseconds, Math.min(millis, timeout));
                if (min > 0) {
                    TimeUnit.MILLISECONDS.sleep(min);
                }
                check();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void check() throws IOException {
        System.out.println("EtsClarityChecker::check");
        boolean isFriday = LocalDate.now().getDayOfWeek() == DayOfWeek.FRIDAY;
        if (isFriday) {
            if(isResolvedToday){
                return;
            }
            Calendar calendar = Calendar.getInstance();
            int currentTimeInHours = calendar.get(Calendar.HOUR_OF_DAY);
            int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
            if (currentTimeInHours >= 10 && currentTimeInHours < 18) {
                int timeout = getTimeout();
                if (timeout > 0) {
                    sleep(timeout, TimeUnit.MINUTES);
                }
                sendNotification(Common.BIG_GENERAL_CHAT_ID);
                System.out.println(new Date().getTime() + "::EtsClarityChecker::TRUE; Hours: " + currentTimeInHours + "; Minutes: " + currentTimeInMinutes);
            }
        } else {
            isResolvedToday = false;
            HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
            for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
                userBooleanEntry.setValue(false);
            }
        }
    }

    private int getTimeout() {
        Calendar calendar = Calendar.getInstance();
        int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
        return 59 - currentTimeInMinutes;
    }

    private void sendNotification(long chatId) {
        String message = getMessage(bot);
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                new InlineKeyboardButton("Resolve").callbackData("ets_resolved")
            }));
        SendResponse execute = bot.execute(request);
        LAST_MESSAGE_ID = execute.message().messageId();
        LAST_MESSAGE_CHAT_ID = chatId;
        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
        commonData.put("LAST_MESSAGE_ID", LAST_MESSAGE_ID);
        commonData.put("LAST_MESSAGE_CHAT_ID", LAST_MESSAGE_CHAT_ID);
        SharedObject.save(COMMON_INT_DATA, commonData);
    }

    public static void updateMessage(TelegramBot bot) {
        try {
            EditMessageText request = new EditMessageText(LAST_MESSAGE_CHAT_ID, LAST_MESSAGE_ID, getMessage(bot))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                    new InlineKeyboardButton("Resolve").callbackData("ets_resolved")
                }));
            bot.execute(request);
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    public static String getMessage(TelegramBot bot) {
        String message = getMessageFromFile("ets_clarity.html");
        String users = getUsers(bot);
        message = message + users;
        return message;
    }

    private static String getUsers(TelegramBot bot) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(Common.BIG_GENERAL_CHAT_ID));
        int count = response.count();
        StringBuilder resolvedUsers = new StringBuilder();
        int resolvedCount = 0;
        if (!users.isEmpty()) {
            for (Map.Entry<User, Boolean> userBooleanEntry : users.entrySet()) {
                User user = userBooleanEntry.getKey();
                Boolean resolved = userBooleanEntry.getValue();
                if (!user.isBot()) {
                    resolvedUsers.append(String.format("%s %s : %b%n", user.firstName(), user.lastName(), resolved));
                }
                if(resolved){
                    resolvedCount++;
                }
            }
        }
        isResolvedToday = resolvedCount == count-1;
        return resolvedUsers.toString() + String.format("%nResolved: %d/%d%n", resolvedCount, count-1);
    }

    public static String getMessageFromFile(String fileUrl) {
        String message = null;
        try {
            message = StringHelper.getFileAsString(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
