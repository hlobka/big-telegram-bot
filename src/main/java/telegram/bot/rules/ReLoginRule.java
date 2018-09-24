package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.time.TimeHelper;
import telegram.bot.commands.ResolveEts;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReLoginRule implements Rule {
    public static final String TRY_TO_RE_LOGIN = "try_to_re_login_";
    private TelegramBot bot;
    private static Map<String,Boolean> statuses = new HashMap<>();

    public ReLoginRule(TelegramBot bot) {
        this.bot = bot;
    }

    public static void tryToRelogin(TelegramBot bot, Throwable e) {
        String message = "Possible Jira Errors: ```" + e.getClass().getSimpleName()+"```";
        long groupId = Common.TEST_FOR_BOT_GROUP_ID;
        String callbackId = TRY_TO_RE_LOGIN + e.hashCode();
        statuses.put(callbackId, false);
        sendMessage(bot, groupId, message, callbackId);
        while (!statuses.get(callbackId)){
            TimeHelper.waitTime(10, TimeUnit.SECONDS);
        }
    }

    private static void sendMessage(TelegramBot bot, long groupId, String message, String calbackId) {
        message = BotHelper.getCuttedMessage(message);
        SendMessage request = new SendMessage(groupId, message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Open jira").url(Common.JIRA.url),
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
            }
            bot.execute(new DeleteMessage(message.chat().id(), message.messageId()));
        }
    }
}
