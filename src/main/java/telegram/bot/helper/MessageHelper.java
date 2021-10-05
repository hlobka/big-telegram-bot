package telegram.bot.helper;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
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

public class MessageHelper {

    public static Message getAnyMessage(Update update) {
        Message result = update.message();
        if (result == null) {
            result = update.editedMessage();
        }
        if (result == null) {
            result = update.channelPost();
        }
        if (result == null) {
            result = update.editedChannelPost();
        }
        return result;
    }
}
