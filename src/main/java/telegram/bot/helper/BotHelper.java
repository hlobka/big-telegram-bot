package telegram.bot.helper;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import telegram.bot.data.TelegramCriteria;

public class BotHelper {
    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode){
        return sendMessage(bot, chatId, message, parseMode, false, false);
    }

    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode, boolean disableWebPagePreview, boolean disableNotification){
        message = getCuttedMessage(message);
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(parseMode)
            .disableWebPagePreview(disableWebPagePreview)
            .disableNotification(disableNotification);
        return bot.execute(request);
    }

    public static String getCuttedMessage(String message) {
        int length = message.length();
        if (length >= TelegramCriteria.MAX_MESSAGE_LENGTH) {
            String notification = String.format("%nПревышина максимальная длина сообщения. %nТекущая %d из допустимых %d", length, TelegramCriteria.MAX_MESSAGE_LENGTH);
            message = message.substring(0, TelegramCriteria.MAX_MESSAGE_LENGTH - notification.length())+notification;
        }
        return message;
    }

    public static BaseResponse alert(TelegramBot bot, String callbackQueryId, String text) {
        return bot.execute(new AnswerCallbackQuery(callbackQueryId)
            .text(text)
            .showAlert(true)
        );
    }
}
