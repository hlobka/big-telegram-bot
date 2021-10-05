package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.MessageHelper;

public class BotSayAnswerRule implements Rule {
    private TelegramBot bot;

    public BotSayAnswerRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
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
        BotHelper.removeMessage(bot, message);
    }

    private void sendMessage(Message message) {
        SendMessage request = new SendMessage(message.chat().id(), message.text().replaceAll("#bot_say", ""))
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(false)
            .disableNotification(false);
        bot.execute(request);
    }
}
