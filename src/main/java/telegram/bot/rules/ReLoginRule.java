package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.logFor;

public class ReLoginRule implements Rule {
    public static final String TRY_TO_RE_LOGIN = "try_to_re_login_";
    private TelegramBot bot;
    private static Map<String, Boolean> statuses = new HashMap<>();

    public ReLoginRule(TelegramBot bot) {
        this.bot = bot;
    }

    public static void tryToRelogin(TelegramBot bot, Throwable e, LoginData jiraLoginData) {
        String message = "Possible Jira Errors: ```" + e.getClass().getSimpleName() + "```";
        String callbackId = TRY_TO_RE_LOGIN + e.hashCode();
        statuses.put(callbackId, false);
        for (ChatData chatData : Common.data.getChatForReport()) {
            sendMessage(bot, chatData.getChatId(), message, callbackId, jiraLoginData);
            e.printStackTrace();
        }
        while (!statuses.get(callbackId)) {
            logFor(ReLoginRule.class, "await 10 seconds for:" + callbackId);
            TimeHelper.waitTime(10, TimeUnit.SECONDS);
        }
        logFor(ReLoginRule.class, "await for:" + callbackId + " End");
    }

    private static void sendMessage(TelegramBot bot, long groupId, String message, String calbackId, LoginData jiraLoginData) {
        message = BotHelper.getCuttedMessage(message);
        SendMessage request = new SendMessage(groupId, message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Open jira").url(jiraLoginData.url),
                new InlineKeyboardButton("Try again").callbackData(calbackId)
            }));
        SendResponse execute = bot.execute(request);
    }

    @Override
    public void run(Update update) {

    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;
        if (isDataPresent) {
            Message message = callbackQuery.message();
            String data = callbackQuery.data();
            if (data.contains(TRY_TO_RE_LOGIN)) {
                statuses.put(data, true);
                logFor(ReLoginRule.class, "await for:" + data + " approved");
                BotHelper.removeMessage(bot, message);
            }
        }
    }
}
