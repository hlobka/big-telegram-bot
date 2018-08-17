package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.UpsourceChecker;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.io.IOException;
import java.util.List;

public class ShowUpsourceReviewCommand implements Command {
    private final TelegramBot bot;

    public ShowUpsourceReviewCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            if(chatData.getChatId() == chatId){
                tryToCheckUpsource(chatData);
            }
        }

        return null;
    }

    private void tryToCheckUpsource(ChatData chatData)  {
        try {
            new UpsourceChecker(bot).check(chatData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
