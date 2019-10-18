package telegram.bot.helper;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import telegram.bot.data.Common;
import telegram.bot.data.TelegramCriteria;
import telegram.bot.data.chat.ChatData;

public class BotHelper {

    public static BaseResponse removeMessage(TelegramBot bot, Message message) {
        return removeMessage(bot, message.chat().id(), message.messageId());
    }

    public static BaseResponse removeMessage(TelegramBot bot, long chatId, int messageId) {
        DeleteMessage request = new DeleteMessage(chatId, messageId);
        return bot.execute(request);
    }

    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode) {
        return sendMessage(bot, chatId, message, parseMode, false, false);
    }

    public static SendResponse sendMessage(TelegramBot bot, long chatId, String message, ParseMode parseMode, boolean disableWebPagePreview, boolean disableNotification) {
        message = getCuttedMessage(message);
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(parseMode)
            .disableWebPagePreview(disableWebPagePreview)
            .disableNotification(disableNotification);
        return bot.execute(request);
    }

    public static BaseResponse editMessage(TelegramBot bot, Long chatId, int messageId, String message) {
        return editMessage(bot, chatId, messageId, message, ParseMode.Markdown, false);
    }

    public static BaseResponse editMessage(TelegramBot bot, Long chatId, int messageId, String message, ParseMode parseMode, boolean disableWebPagePreview) {
        message = getCuttedMessage(message);
        EditMessageText request = new EditMessageText(chatId, messageId, message)
            .parseMode(parseMode)
            .disableWebPagePreview(disableWebPagePreview);
        return bot.execute(request);
    }

    public static String getCuttedMessage(String message) {
        int length = message.length();
        if (length >= TelegramCriteria.MAX_MESSAGE_LENGTH) {
            String notification = String.format("%nПревышина максимальная длина сообщения. %nТекущая %d из допустимых %d", length, TelegramCriteria.MAX_MESSAGE_LENGTH);
            message = message.substring(0, TelegramCriteria.MAX_MESSAGE_LENGTH - notification.length()) + notification;
        }
        return message;
    }

    public static BaseResponse alert(TelegramBot bot, String callbackQueryId, String text) {
        return bot.execute(new AnswerCallbackQuery(callbackQueryId)
            .text(text)
            .showAlert(true)
        );
    }

    public static String clearForHtmlMessages(String message) {
        return message
            .replaceAll("&laquo;", "«")
            .replaceAll("&raquo;", "»")
            .replaceAll("&quot;", "\"")
            .replaceAll("<br ?/>", "");
    }

    public static void logError(TelegramBot bot, String message) {
        for (ChatData chatData : Common.data.getChatForReport()) {
            sendMessage(bot, chatData.getChatId(), getCuttedMessage(clearForHtmlMessages(message)), ParseMode.Markdown);
        }

    }

    public static String getLinkOnUser(User user) {
        return getLinkOnUser(user, ParseMode.Markdown);
    }

    public static String getLinkOnUser(User user, ParseMode parseMode) {
        return getLinkOnUser(user, user.firstName() + " " + user.lastName(), parseMode);
    }

    public static String getLinkOnUser(User user, String userName, ParseMode parseMode) {
        String uri = "tg://user?id=" + user.id();
        return getLink(uri, userName, parseMode);
    }

    public static String getLink(String uri, String uriName) {
        return getLink(uri, uriName, ParseMode.Markdown);
    }

    public static String getLink(String uri, String uriName, ParseMode parseMode) {
        if (parseMode == ParseMode.Markdown) {
            return "[" + uriName + "](" + uri + ")";
        }
        return "<a href=\"" + uri + "\">" + uriName + "</a>";
    }
}
