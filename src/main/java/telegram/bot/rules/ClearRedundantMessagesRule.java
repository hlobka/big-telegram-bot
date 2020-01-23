package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.response.SendResponse;
import helper.logger.ConsoleLogger;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.data.chat.ChatFilter;
import telegram.bot.helper.BotHelper;

import java.util.concurrent.TimeUnit;

public class ClearRedundantMessagesRule implements Rule {
    private final TelegramBot bot;

    public ClearRedundantMessagesRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        Long chatId = message.chat().id();
        if (message.from().isBot()) {
            return;
        }
        if (Common.data.hasChatData(chatId)) {
            ChatData chatData = Common.data.getChatData(chatId);
            ChatFilter chatFilter = chatData.getChatFilter();
            if (chatFilter.getIsActive() && text.matches(chatFilter.getRegexp())) {
                updateAndRemoveFilteredMessage(message, text, chatId, chatFilter);
            }
        }
    }

    private void updateAndRemoveFilteredMessage(Message message, String text, Long chatId, ChatFilter chatFilter) {
        BotHelper.removeMessage(bot, message);
        String time = TimeHelper.getMinutesAsStringTime(chatFilter.getDelayInMinutes());
        String newMessage = text + "\n```Данное сообщение будет удалено через: " + time + "```";
        SendResponse sendResponse = BotHelper.sendMessage(bot, chatId, newMessage, ParseMode.Markdown);
        if (sendResponse.isOk()) {
            new Thread(() -> {
                TimeHelper.waitTime(chatFilter.getDelayInMinutes(), TimeUnit.MINUTES);
                try {
                    TimeUnit.MILLISECONDS.sleep(chatFilter.getDelayInMinutes());
                } catch (InterruptedException e) {
                    ConsoleLogger.logErrorFor(this, e);
                }
                BotHelper.removeMessage(bot, sendResponse.message());
            }).start();
        }
    }

}