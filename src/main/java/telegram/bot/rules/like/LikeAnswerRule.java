package telegram.bot.rules.like;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import telegram.bot.helper.BotHelper;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import telegram.bot.data.Common;
import telegram.bot.helper.MessageHelper;
import telegram.bot.rules.Rule;

import java.util.HashMap;

public class LikeAnswerRule implements Rule {
    private TelegramBot bot;
    private HashMap<Integer, Like> listOfLikes;

    public LikeAnswerRule(TelegramBot bot) {
        this.bot = bot;
        listOfLikes = SharedObject.loadMap(Common.LIKED_POSTS, new HashMap<>());
    }

    @Override
    public boolean guard(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        if (message == null) {
            return false;
        }
        boolean isBot = message.from() != null && message.from().isBot();
        String text = message.text() == null ? "" : message.text();
        boolean isLikeMessage = text.toLowerCase().contains("#like");
        return !isBot && isLikeMessage;
    }

    @Override
    public void run(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        String text = message.text() == null ? "" : message.text();
        if (text.toLowerCase().contains("#like")) {
            BotHelper.removeMessage(bot, message);
            sendMessage(message);
        }
    }

    private void sendMessage(Message message) {
        SendMessage request = new SendMessage(message.chat().id(), "Like it: " + message.text().replaceAll("#like", ""))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .disableNotification(false)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Like 👍🏻").callbackData("like_0"),
                        new InlineKeyboardButton("DisLike 👎🏻").callbackData("dislike_0")
                }));
        if (message.replyToMessage() != null) {
            request.replyToMessageId(message.replyToMessage().messageId());
        }
        SendResponse execute = bot.execute(request);
        listOfLikes.put(getLikeKey(execute.message()), new Like());
        SharedObject.save(Common.LIKED_POSTS, listOfLikes);
    }

    private int getLikeKey(Message message) {
        return (message.chat().id() + "_" + message.messageId()).hashCode();
    }

    private void updateMessage(Message message, Integer numberOfLikes, Integer numberOfDisLikes) {
        try {
            EditMessageText request = new EditMessageText(message.chat().id(), message.messageId(), message.text())
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                            new InlineKeyboardButton(String.format("Like %d 👍🏻", numberOfLikes)).callbackData("like_" + numberOfLikes),
                            new InlineKeyboardButton(String.format("DisLike %d 👎🏻", numberOfDisLikes)).callbackData("dislike_" + numberOfDisLikes)
                    }));
            bot.execute(request);
        } catch (RuntimeException e) {
            ConsoleLogger.logErrorFor(this, e);
        }
    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;

        if (isDataPresent) {
            Message message = callbackQuery.message();
            if (message != null) {
                String data = callbackQuery.data();
                if (data.contains("like_")) {
                    Integer uniqueMessageId = getLikeKey(callbackQuery.message());
                    Like like = listOfLikes.getOrDefault(uniqueMessageId, new Like());
                    if (!listOfLikes.containsKey(uniqueMessageId)) {
                        listOfLikes.put(uniqueMessageId, like);
                    }
                    Long whoId = callbackQuery.from().id();
                    if (data.contains("dislike_") && !like.usersWhoDisLiked.contains(whoId)) {
                        like.usersWhoDisLiked.add(whoId);
                        like.usersWhoLiked.remove(whoId);
                    } else if (!like.usersWhoLiked.contains(whoId)) {
                        like.usersWhoLiked.add(whoId);
                        like.usersWhoDisLiked.remove(whoId);
                    } else {
                        like.usersWhoDisLiked.add(whoId);
                        like.usersWhoLiked.remove(whoId);
                    }
                    int numberOfLikes = like.usersWhoLiked.size();
                    int numberOfDisLikes = like.usersWhoDisLiked.size();
                    listOfLikes.put(uniqueMessageId, like);
                    SharedObject.save(Common.LIKED_POSTS, listOfLikes);
                    updateMessage(message, numberOfLikes, numberOfDisLikes);
                }
            }
        }
    }
}
