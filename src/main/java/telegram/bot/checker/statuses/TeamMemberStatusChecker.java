package telegram.bot.checker.statuses;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.SendResponse;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.logFor;

public class TeamMemberStatusChecker extends Thread {
    private final TelegramBot bot;
    private final long chatId;
    private final List<DayOfWeek> daysToCheck;
    private static final List<MemberStatus[]> workStatuses;
    private int lastMessageChatId;

    static {
        workStatuses = new ArrayList<>();
        workStatuses.add(MemberWorkStatus.values());
        workStatuses.add(MemberFunStatus.values());
    }

    private final HashMap<Integer, String> telegramUserStatus;

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        new TeamMemberStatusChecker(bot, -1001133804517L, Collections.singletonList(DayOfWeek.SATURDAY)).run();
    }

    public TeamMemberStatusChecker(TelegramBot bot, long chatId, List<DayOfWeek> daysToCheck) {
        this.bot = bot;
        this.chatId = chatId;
        this.daysToCheck = daysToCheck;
        String chatName = Common.data.getChatData(chatId).getChatName();
        String uniqueName = chatName + "_" + chatId;
        String sharedObjectPath = Common.getDefaultSharedObjectPath(uniqueName);
        telegramUserStatus = SharedObject.loadMap(sharedObjectPath, new HashMap<>());
        bot.setUpdatesListener(list -> {
            handleBotUpdates(list);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleBotUpdates(List<Update> updateList) {
        for (Update update : updateList) {
            CallbackQuery callbackQuery = update.callbackQuery();
            boolean isCallBackAvailable = callbackQuery != null && callbackQuery.data() != null;
            if (isCallBackAvailable && callbackQuery.message().chat().id() == chatId) {
                String data = callbackQuery.data();
                String regex = "SetMemberStatus:(\\w+):(.*)";
                if (data.matches(regex)) {
                    String type = StringHelper.getRegString(data, regex, 1);
                    String status = StringHelper.getRegString(data, regex, 2);
                    if (status.equals("?")) {
                        showHelp(callbackQuery.id(), type);
                    } else {
                        setMemberStatus(status, callbackQuery.from());
                    }
                }
            }
        }
    }

    private void setMemberStatus(String status, User from) {
        telegramUserStatus.put(from.id(), status);
        update();
    }

    private void showHelp(String callbackQueryId, String type) {
        BotHelper.alert(bot, callbackQueryId, getHelpMessage(type));
    }

    private String getHelpMessage(String type) {
        for (MemberStatus[] workStatus : workStatuses) {
            if (workStatus[workStatus.length - 1].type().equals(type)) {
                return getHelpMessage(workStatus);
            }
        }
        return "WHAT?";
    }

    private String getHelpMessage(MemberStatus[] workStatus) {
        StringBuilder message = new StringBuilder(workStatus[0].type() + " status Help: \n");
        for (MemberStatus status : workStatus) {
            message.append(status.getStatus());
            message.append(" - ");
            message.append(status.name());
            message.append("\n");
        }
        return message.toString();
    }

    @Override
    public void run() {
        super.run();
        logFor(this, "start");
        while (true) {
            check();
            sleepToNextCheck();
        }
    }

    private void update() {
        String message = getMessage();
        EditMessageText request = new EditMessageText(chatId, lastMessageChatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .replyMarkup(new InlineKeyboardMarkup(getInlineKeyboardButtons()));
        bot.execute(request);
    }

    private void check() {
        String message = getMessage();
        removeLastNotification(bot);
        sendMessage(bot, message);
    }

    private void sendMessage(TelegramBot bot, String message) {
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(getInlineKeyboardButtons()));
        SendResponse execute = bot.execute(request);
        lastMessageChatId = execute.message().messageId();
//        lastMessageChatId = chatId;
//        HashMap<String, Number> commonData = SharedObject.loadMap(COMMON_INT_DATA, new HashMap<String, Number>());
//        commonData.put("LAST_MESSAGE_ID", LAST_MESSAGE_ID);
//        commonData.put("LAST_MESSAGE_CHAT_ID", lastMessageChatId);
//        SharedObject.save(COMMON_INT_DATA, commonData);
        bot.execute(new PinChatMessage(chatId, lastMessageChatId));
    }

    private InlineKeyboardButton[][] getInlineKeyboardButtons() {
        InlineKeyboardButton[][] result = new InlineKeyboardButton[workStatuses.size()][];
        for (int i = 0; i < workStatuses.size(); i++) {
            InlineKeyboardButton[] resultIn = new InlineKeyboardButton[workStatuses.get(i).length];
            for (int j = 0; j < workStatuses.get(i).length; j++) {
                MemberStatus memberStatus = workStatuses.get(i)[j];
                String status = memberStatus.getStatus();
                String type = memberStatus.type();
                resultIn[j] = new InlineKeyboardButton(status)
                    .callbackData(String.format("SetMemberStatus:%s:%s", type, status));
            }

            result[i] = resultIn;
        }
        return result;
    }

    private void removeLastNotification(TelegramBot bot) {
        if (lastMessageChatId != -1) {
            bot.execute(new UnpinChatMessage(chatId));
            BotHelper.removeMessage(bot, chatId, lastMessageChatId);
        }
    }

    private String getMessage() {
        StringBuilder result = new StringBuilder();
        result.append("Эксперементальный статус стенд: \n");
        for (Map.Entry<Integer, String> integerStringEntry : telegramUserStatus.entrySet()) {
            User user = getUser(integerStringEntry);
            result.append(String.format("%n%s %s: %s", user.firstName(), user.lastName(), getUserStatus(user)));
        }

        return result.toString();
    }

    private String getUserStatus(User user) {
        return telegramUserStatus.getOrDefault(user.id(), "🍏");
    }

    private User getUser(Map.Entry<Integer, String> integerStringEntry) {
        Integer userId = integerStringEntry.getKey();
        GetChatMemberResponse execute = bot.execute(new GetChatMember(chatId, userId));
        return execute.chatMember().user();
    }

    private void sleepToNextCheck() {
        long timeout = TimeUnit.MINUTES.toMillis(getTimeout());
        long oneMinuteInMilliseconds = TimeUnit.MINUTES.toMillis(1);
        long min = Math.max(oneMinuteInMilliseconds, Math.min(0, timeout));
        if (min > 0) {
            sleep(min, TimeUnit.MILLISECONDS);
        }
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

    private long getTimeout() {
        Calendar calendar = Calendar.getInstance();
        int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
        return 59 - currentTimeInMinutes;
    }

}

