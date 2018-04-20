package telegram.bot.helper;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class BotHelper {
    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode){
        return sendMessage(bot, chatId, message, parseMode, false, false);
    }

    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode, boolean disableWebPagePreview, boolean disableNotification){
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(parseMode)
            .disableWebPagePreview(disableWebPagePreview)
            .disableNotification(disableNotification);
        return bot.execute(request);
    }
}
