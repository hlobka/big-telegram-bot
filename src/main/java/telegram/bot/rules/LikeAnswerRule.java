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

public class LikeAnswerRule implements Rule {
    private TelegramBot bot;
    private Map<Integer, Integer> listOfLikes;

    public LikeAnswerRule(TelegramBot bot) {
        this.bot = bot;
        listOfLikes = new HashMap<>();
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.from().isBot()) {
            return;
        }
        if(text.toLowerCase().contains("#like")){
            removeMessage(message);
            sendMessage(message);
        }
    }

    private void removeMessage(Message message) {
        DeleteMessage request = new DeleteMessage(message.chat().id(), message.messageId());
        BaseResponse execute = bot.execute(request);
    }

    private void sendMessage(Message message) {
        SendMessage request = new SendMessage(message.chat().id(), "Like it: " + message.text().replaceAll("#like", ""))
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Like 👍🏻").callbackData("like_0")
            }));
        SendResponse execute = bot.execute(request);
        listOfLikes.put(execute.message().messageId(), 0);
    }

    private void updateMessage(Message message, Integer numberOfLikes) {
        try {
            EditMessageText request = new EditMessageText(message.chat().id(), message.messageId(), message.text())
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                    new InlineKeyboardButton(String.format("Like %d 👍🏻", numberOfLikes)).callbackData("like_"+numberOfLikes)
                }));
            bot.execute(request);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;

        if (isDataPresent){
            Message message = callbackQuery.message();
            if(message != null) {
                String data = callbackQuery.data();
                if (data.contains("like_")) {
                    String numberOfLikesAsString = StringHelper.getRegString(data, "like_(\\d+)");
                    int numberOfLikes = listOfLikes.getOrDefault(message.messageId(), Integer.parseInt(numberOfLikesAsString)) + 1;
                    listOfLikes.put(message.messageId(), numberOfLikes);
                    updateMessage(message, numberOfLikes);
                }
            }
        }
    }
}
