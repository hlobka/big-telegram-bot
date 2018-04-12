package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import helper.string.StringHelper;

import java.util.HashMap;
import java.util.Map;

public class BotSayAnswerRule implements Rule {
    private TelegramBot bot;

    public BotSayAnswerRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.from().isBot()) {
            return;
        }
        if(text.toLowerCase().contains("#bot_say")){
            removeMessage(message);
            sendMessage(message);
        }
    }

    private void removeMessage(Message message) {
        DeleteMessage request = new DeleteMessage(message.chat().id(), message.messageId());
        bot.execute(request);
    }

    private void sendMessage(Message message) {
        SendMessage request = new SendMessage(message.chat().id(), message.text().replaceAll("#bot_say", ""))
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(false)
            .disableNotification(false);
        bot.execute(request);
    }
}
